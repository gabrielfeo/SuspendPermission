package com.gabrielfeo.permissions.verifier

interface PermissionVerifier {
    /**
     * Ensure all [permissions] are currently granted.
     * @throws PermissionsDeniedException if one or more permissions are denied
     */
    suspend fun ensureGranted(permissions: Array<out String>)

    /**
     * Ensure all [requestedPermissions] have been granted in [grantResults].
     * @throws PermissionsDeniedException if one or more permissions have been denied
     */
    suspend fun ensureGrantedInResults(grantResults: IntArray, requestedPermissions: Array<out String>)
}

suspend fun PermissionVerifier.ensureGranted(
    vararg permission: String
) = ensureGranted(permissions = permission)

suspend fun PermissionVerifier.ensureGrantedInResults(
    grantResults: IntArray,
    vararg requestedPermissions: String
) = ensureGranted(permissions = requestedPermissions)