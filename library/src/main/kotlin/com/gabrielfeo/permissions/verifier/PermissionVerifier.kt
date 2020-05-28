package com.gabrielfeo.permissions.verifier

interface PermissionVerifier {
    /**
     * Verify permissions are currently granted.
     * @throws PermissionsDeniedException if one or more permissions are denied
     */
    suspend fun ensureGranted(permissions: Array<out String>)

    /**
     * Verify permissions have been granted in [grantResults].
     * @throws PermissionsDeniedException if one or more permissions have been denied
     */
    suspend fun ensureGrantedInResults(permissions: Array<out String>, grantResults: IntArray)
}