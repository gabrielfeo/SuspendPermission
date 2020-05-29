@file:Suppress("SimpleRedundantLet")

package com.gabrielfeo.permissions.requester

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.gabrielfeo.permissions.verifier.AndroidPermissionVerifier
import com.gabrielfeo.permissions.verifier.PermissionAssurer
import com.gabrielfeo.permissions.verifier.PermissionsDeniedException.PermissionsCurrentlyDeniedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PermissionsRequesterFragment : Fragment() {

    private var pendingPermissionRequests: MutableMap<Int, PermissionRequest> = HashMap(1)
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
            permissionAssurer = AndroidPermissionVerifier(
                requireContext(),
                isPermanentlyDenied = { permission -> shouldShowRequestPermissionRationale(permission) },
                dispatcher = Dispatchers.Main // TODO Background?
            )
            runCatching { permissionAssurer.ensureGranted(this, permissions) }
                .onSuccess { continuation.resume(Unit) }
                .recover { exception ->
                    if (exception is PermissionsCurrentlyDeniedException) {
                        pendingPermissionRequests[requestCode] = PermissionRequest(exception.deniedPermissions, continuation)
                        requestPermissions(exception.deniedPermissions, requestCode)
                    } else continuation.resumeWithException(exception)
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
        val permissionRequest = pendingPermissionRequests[requestCode] ?: return
        lifecycleScope.launch {
            runCatching { permissionAssurer.ensureGrantedInResults(this, grantResults, permissionRequest.permissions) }
                .onSuccess { permissionRequest.continuation.resume(Unit) }
                .onFailure { exception -> permissionRequest.continuation.resumeWithException(exception) }
        }
    }

}

private const val TAG = "permissionsRequester"

suspend fun FragmentActivity.requestPermissionsAsync(
    permissions: Array<String>,
    requestCode: Int
) = suspendCancellableCoroutine<Unit> { continuation ->
    supportFragmentManager.commit {
        add(PermissionsRequesterFragment(), TAG)
        runOnCommit {
            lifecycleScope.launch {
                (supportFragmentManager.findFragmentByTag(TAG) as? PermissionsRequesterFragment)?.let {
                    it.requestPermissionsWith(continuation, permissions, requestCode)
                }
            }
        }
    }
}

suspend fun Fragment.requestPermissionsAsync(
    permissions: Array<String>,
    requestCode: Int
) = suspendCancellableCoroutine<Unit> { continuation ->
    childFragmentManager.commit {
        add(PermissionsRequesterFragment(), TAG)
        runOnCommit {
            viewLifecycleOwnerLiveData.value?.lifecycleScope?.launch {
                (childFragmentManager.findFragmentByTag(TAG) as? PermissionsRequesterFragment)?.let {
                    it.requestPermissionsWith(continuation, permissions, requestCode)
                }
            }
        }
    }
}