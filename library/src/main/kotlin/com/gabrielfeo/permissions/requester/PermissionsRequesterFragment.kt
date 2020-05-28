@file:Suppress("SimpleRedundantLet")

package com.gabrielfeo.permissions.requester

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.gabrielfeo.permissions.verifier.PermissionVerifier
import com.gabrielfeo.permissions.verifier.PermissionsDeniedException.PermissionsCurrentlyDeniedException
import com.gabrielfeo.permissions.verifier.PermissionsDeniedException.PermissionsPermanentlyDeniedException
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PermissionsRequesterFragment(
    permissionVerifier: PermissionVerifier
) : Fragment(), PermissionVerifier by permissionVerifier {

    private var pendingPermissionRequests: MutableMap<Int, PermissionRequest> = HashMap(1)

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
            runCatching { ensureGranted(permissions) }
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
            runCatching { ensureGrantedInResults(grantResults, permissionRequest.permissions) }
                .onSuccess { permissionRequest.continuation.resume(Unit) }
                .onFailure { exception ->
                    val actualException = mapExceptionOf(permissionRequest, exception)
                    permissionRequest.continuation.resumeWithException(actualException)
                }
        }
    }

    private fun mapExceptionOf(
        permissionRequest: PermissionRequest,
        exception: Throwable
    ): Throwable = when (exception) {
        is PermissionsCurrentlyDeniedException -> {
            val permanentlyDeniedPermissions = exception.deniedPermissions
                .filterNot { permission -> shouldShowRequestPermissionRationale(permission) }
            when {
                permanentlyDeniedPermissions.isNotEmpty() -> PermissionsPermanentlyDeniedException(
                    permanentlyDeniedPermissions.toTypedArray(), // TODO Write Array-returning filter
                    exception.deniedPermissions,
                    allRequestedAreDenied = exception.deniedPermissions.size == permissionRequest.permissions.size
                )
                else -> exception.clone(
                    allPermissionWereDenied = exception.deniedPermissions.size == permissionRequest.permissions.size
                )
            }
        }
        else -> exception
    }

    private fun PermissionsCurrentlyDeniedException.clone(
        deniedPermissions: Array<out String> = this.deniedPermissions,
        allPermissionWereDenied: Boolean = this.allRequestedAreDenied
    ) = PermissionsCurrentlyDeniedException(deniedPermissions, allRequestedAreDenied)
    // TODO Debug. IDE says this will always use instance allPermissionWereDenied (WTF)

}

private const val TAG = "permissionsRequester"

suspend fun FragmentActivity.requestPermissionsAsync(
    permissions: Array<String>,
    requestCode: Int
) = suspendCancellableCoroutine<Unit> { continuation ->
    supportFragmentManager.commit {
        add(PermissionsRequesterFragment::class.java, null, TAG)
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
        add(PermissionsRequesterFragment::class.java, null, TAG)
        runOnCommit {
            viewLifecycleOwnerLiveData.value?.lifecycleScope?.launch {
                (childFragmentManager.findFragmentByTag(TAG) as? PermissionsRequesterFragment)?.let {
                    it.requestPermissionsWith(continuation, permissions, requestCode)
                }
            }
        }
    }
}