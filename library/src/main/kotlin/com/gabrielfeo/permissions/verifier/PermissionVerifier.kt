package com.gabrielfeo.permissions.verifier

import kotlinx.coroutines.CoroutineScope

interface PermissionVerifier {
    /**
     * Ensure all [permissions] are currently granted.
     * @throws PermissionsDeniedException if one or more permissions are denied
     */
    suspend fun ensureGranted(scope: CoroutineScope, permissions: Array<out String>)

    /**
     * Ensure all [requestedPermissions] have been granted in [grantResults].
     * @throws PermissionsDeniedException if one or more permissions have been denied
     */
    suspend fun ensureGrantedInResults(
        scope: CoroutineScope,
        grantResults: IntArray,
        requestedPermissions: Array<out String>
    )
}

suspend fun PermissionVerifier.ensureGranted(
    scope: CoroutineScope,
    vararg permissions: String
) = ensureGranted(scope, permissions)

suspend fun PermissionVerifier.ensureGrantedInResults(
    scope: CoroutineScope,
    grantResults: IntArray,
    vararg requestedPermissions: String
) = ensureGrantedInResults(scope, grantResults, requestedPermissions)