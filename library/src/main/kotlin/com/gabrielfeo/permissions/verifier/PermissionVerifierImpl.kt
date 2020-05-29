package com.gabrielfeo.permissions.verifier

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.annotation.RestrictTo.Scope.LIBRARY
import androidx.core.content.PermissionChecker
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import com.gabrielfeo.permissions.verifier.PermissionsDeniedException.PermissionsCurrentlyDeniedException
import com.gabrielfeo.permissions.verifier.PermissionsDeniedException.PermissionsPermanentlyDeniedException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async

class AndroidPermissionVerifier(
    context: Context,
    isPermanentlyDenied: (permission: String) -> Boolean,
    dispatcher: CoroutineDispatcher
) : PermissionVerifierImpl(
    getPermissionResult = { permission -> PermissionChecker.checkCallingOrSelfPermission(context, permission) },
    mapResult = { permission: String, result: Int ->
        when {
            result == PERMISSION_GRANTED -> GRANTED
            isPermanentlyDenied(permission) -> PERMANENTLY_DENIED
            else -> CURRENTLY_DENIED
        }
    },
    dispatcher = dispatcher
)


@RestrictTo(LIBRARY)
open class PermissionVerifierImpl internal constructor(
    private val getPermissionResult: (permission: String) -> Int,
    private val mapResult: (permission: String, platformResult: Int) -> Int,
    private val dispatcher: CoroutineDispatcher
) : PermissionVerifier {

    internal companion object PermissionResults {
        const val GRANTED = 0
        const val CURRENTLY_DENIED = 1
        const val PERMANENTLY_DENIED = 2
    }

    override suspend fun ensureGranted(
        scope: CoroutineScope,
        permissions: Array<out String>
    ) {
        val resultsByPermission = permissions.associate { permission ->
            permission to scope.async(dispatcher) { getPermissionResult(permission) }
        }
        ensureAllGranted(resultsByPermission)
    }

    override suspend fun ensureGrantedInResults(
        scope: CoroutineScope,
        grantResults: IntArray,
        requestedPermissions: Array<out String>
    ) {
        val resultsByPermission: MutableMap<String, Deferred<Int>> = HashMap()
        requestedPermissions.forEachIndexed { i, permission ->
            resultsByPermission[permission] = scope.async(dispatcher) { mapResult(permission, grantResults[i]) }
        }
        ensureAllGranted(resultsByPermission)
    }

    private suspend fun ensureAllGranted(resultsByPermission: Map<String, Deferred<Int>>) {
        val permanentlyDeniedPermissions = ArrayList<String>()
        val currentlyDeniedPermissions = ArrayList<String>()
        val deniedPermissions = ArrayList<String>()
        resultsByPermission.forEach { (permission, asyncResult) ->
            when (mapResult(permission, asyncResult.await())) {
                CURRENTLY_DENIED -> {
                    deniedPermissions.add(permission)
                    currentlyDeniedPermissions.add(permission)
                }
                PERMANENTLY_DENIED -> {
                    deniedPermissions.add(permission)
                    permanentlyDeniedPermissions.add(permission)
                }
            }
        }
        if (deniedPermissions.isNotEmpty()) {
            throwExceptionFor(
                resultsByPermission.keys.toTypedArray(),
                deniedPermissions,
                currentlyDeniedPermissions,
                permanentlyDeniedPermissions
            )
        }
    }

    private fun throwExceptionFor(
        permissions: Array<out String>,
        deniedPermissions: List<String>,
        currentlyDeniedPermissions: List<String>,
        permanentlyDeniedPermissions: List<String>
    ) {
        if (permanentlyDeniedPermissions.isEmpty()) {
            throw PermissionsCurrentlyDeniedException(
                deniedPermissions.toTypedArray(),
                allRequestedAreDenied = deniedPermissions.size == permissions.size
            )
        } else {
            throw PermissionsPermanentlyDeniedException(
                permanentlyDeniedPermissions.toTypedArray(), // TODO Review
                currentlyDeniedPermissions.toTypedArray(),
                deniedPermissions.toTypedArray(),
                allRequestedAreDenied = deniedPermissions.size == permissions.size,
                allRequestedArePermanentlyDenied = permanentlyDeniedPermissions.size == permissions.size
            )
        }
    }

}