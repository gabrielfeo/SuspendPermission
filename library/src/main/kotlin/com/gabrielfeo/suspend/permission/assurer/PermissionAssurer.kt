package com.gabrielfeo.suspend.permission.assurer

import kotlinx.coroutines.CoroutineScope

/**
 * Ensures permissions are granted.
 */
internal interface PermissionAssurer {
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