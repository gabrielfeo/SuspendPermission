package com.gabrielfeo.permissions.verifier

import android.content.Context
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import com.gabrielfeo.permissions.verifier.PermissionsDeniedException.PermissionsCurrentlyDeniedException
import kotlinx.coroutines.*

class PermissionVerifierImpl(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher
) : PermissionVerifier {

    override suspend fun ensureGranted(permissions: Array<out String>) {
        coroutineScope scope@{
            val results = permissions
                .map { androidPermission -> checkPermissionAsync(androidPermission) }
                .awaitAll()
            val deniedPermissions = permissions.filterIndexed { i, _ -> results[i] != PERMISSION_GRANTED }
            if (deniedPermissions.isNotEmpty()) {
                throwExceptionFor(permissions, deniedPermissions)
            }
        }
    }

    override suspend fun ensureGrantedInResults(permissions: Array<out String>, grantResults: IntArray) {
        val deniedPermissions = permissions.filterIndexed { i, _ -> grantResults[i] != PERMISSION_GRANTED }
        if (deniedPermissions.any()) {
            throwExceptionFor(permissions, deniedPermissions)
        }
    }

    private fun CoroutineScope.checkPermissionAsync(androidPermission: String): Deferred<Int> =
        async(dispatcher) { PermissionChecker.checkCallingOrSelfPermission(context, androidPermission) }

    private fun throwExceptionFor(
        permissions: Array<out String>,
        deniedPermissions: List<String>
    ) {
        throw PermissionsCurrentlyDeniedException(
            deniedPermissions.toTypedArray(),
            allRequestedAreDenied = deniedPermissions.size == permissions.size
        )
    }

}