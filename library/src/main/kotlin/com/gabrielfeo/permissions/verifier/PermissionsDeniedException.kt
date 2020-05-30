package com.gabrielfeo.permissions.verifier

/**
 * Requested or verified permissions have been denied.
 *
 * @property deniedPermissions All denied permissions. Both [currentlyDeniedPermissions] and [permanentlyDeniedPermissions].
 * @property currentlyDeniedPermissions Currently denied permissions, which can be requested once more.
 * @property permanentlyDeniedPermissions Permanently denied permissions, which mustn't be requested again.
 */
class PermissionsDeniedException(
    val deniedPermissions: Array<out String>,
    val permanentlyDeniedPermissions: Array<out String>,
    val currentlyDeniedPermissions: Array<out String>
) : RuntimeException("Requested permissions have been denied")