package com.gabrielfeo.permissions.verifier

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope.LIBRARY
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import com.gabrielfeo.permissions.verifier.PermissionsDeniedException.PermissionsCurrentlyDeniedException
import kotlinx.coroutines.*

class AndroidPermissionVerifier(
    context: Context,
    dispatcher: CoroutineDispatcher
) : PermissionVerifierImpl(
    getPermissionResult = { permission -> PermissionChecker.checkCallingOrSelfPermission(context, permission) },
    isResultGrantedResult = { result: Int -> result == PERMISSION_GRANTED },
    dispatcher = dispatcher
)


@RestrictTo(LIBRARY)
open class PermissionVerifierImpl internal constructor(
    private val getPermissionResult: (permission: String) -> Int,
    private val isResultGrantedResult: (result: Int) -> Boolean,
    private val dispatcher: CoroutineDispatcher
) : PermissionVerifier {

    override suspend fun ensureGranted(scope: CoroutineScope, permissions: Array<out String>) {
        val results = permissions
            .map { androidPermission -> getPermissionResultAsync(scope, androidPermission) }
            .awaitAll()
        val deniedPermissions = permissions.filterIndexed { i, _ -> isResultGrantedResult(results[i]) }
        if (deniedPermissions.isNotEmpty()) {
            throwExceptionFor(permissions, deniedPermissions)
        }
    }

    override suspend fun ensureGrantedInResults(
        scope: CoroutineScope,
        grantResults: IntArray,
        requestedPermissions: Array<out String>
    ) {
        val deniedPermissions = requestedPermissions.filterIndexed { i, _ -> isResultGrantedResult(grantResults[i]) }
        if (deniedPermissions.any()) {
            throwExceptionFor(requestedPermissions, deniedPermissions)
        }
    }

    private fun getPermissionResultAsync(scope: CoroutineScope, androidPermission: String): Deferred<Int> =
        scope.async(dispatcher) { getPermissionResult(androidPermission) }

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