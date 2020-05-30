@file:Suppress("SimpleRedundantLet")

package com.gabrielfeo.suspend.permission.requester

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.gabrielfeo.suspend.permission.assurer.AndroidPermissionAssurer
import com.gabrielfeo.suspend.permission.assurer.PermissionAssurer
import com.gabrielfeo.suspend.permission.assurer.PermissionsDeniedException
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

private const val TAG = "permissionsRequester"

suspend fun FragmentActivity.requestPermissionsAsync(
    permissions: Array<String>,
    requestCode: Int
) = suspendCancellableCoroutine<Unit> { continuation ->
    supportFragmentManager.commit {
        add(PermissionRequesterFragment(), TAG)
        runOnCommit {
            lifecycleScope.launch {
                val requester = supportFragmentManager.findFragmentByTag(TAG) as PermissionRequesterFragment
                requester.requestPermissionsWith(continuation, permissions, requestCode)
            }
        }
    }
}

suspend fun Fragment.requestPermissionsAsync(
    permissions: Array<String>,
    requestCode: Int
) = suspendCancellableCoroutine<Unit> { continuation ->
    childFragmentManager.commit {
        add(PermissionRequesterFragment(), TAG)
        runOnCommit {
            viewLifecycleOwner.lifecycleScope.launch {
                val requester = childFragmentManager.findFragmentByTag(TAG) as PermissionRequesterFragment
                requester.requestPermissionsWith(continuation, permissions, requestCode)
            }
        }
    }
}

class PermissionRequesterFragment : Fragment() {

    private var pendingRequests: MutableMap<Int, PermissionRequest> = HashMap(1)
    private lateinit var permissionAssurer: PermissionAssurer

    private class PermissionRequest(
        val permissions: Array<out String>,
        val continuation: Continuation<Unit>
    )

    internal suspend fun requestPermissionsWith(
        continuation: Continuation<Unit>,
        permissions: Array<String>,
        requestCode: Int
    ) {
        lifecycleScope.launchWhenCreated {
            permissionAssurer = AndroidPermissionAssurer(
                requireContext(),
                isPermanentlyDenied = { permission -> shouldShowRequestPermissionRationale(permission) }
            )
            runCatching { permissionAssurer.ensureGranted(this, permissions) }
                .onSuccess { continuation.resume(Unit) }
                .recover { exception ->
                    if (exception is PermissionsDeniedException) {
                        pendingRequests[requestCode] = PermissionRequest(exception.allDenied, continuation)
                        requestPermissions(exception.allDenied, requestCode)
                    } else {
                        continuation.resumeWithException(exception)
                    }
                }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        retainInstance = false
        super.onCreate(savedInstanceState)
    }

    @Suppress("ThrowableNotThrown")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        val permissionRequest = pendingRequests[requestCode] ?: return
        lifecycleScope.launch {
            runCatching { permissionAssurer.ensureGrantedInResults(this, grantResults, permissionRequest.permissions) }
                .onSuccess { permissionRequest.continuation.resume(Unit) }
                .onFailure { exception -> permissionRequest.continuation.resumeWithException(exception) }
        }
    }

}