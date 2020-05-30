package com.gabrielfeo.suspend.permission.assurer

/**
 * Requested or verified permissions have been denied.
 *
 * @property allDenied All denied permissions. Both [currentlyDenied] and [permanentlyDenied].
 * @property currentlyDenied Currently denied permissions, which can be requested once more.
 * @property permanentlyDenied Permanently denied permissions, which mustn't be requested again.
 */
class PermissionsDeniedException(
    val allDenied: Array<out String>,
    val permanentlyDenied: Array<out String>,
    val currentlyDenied: Array<out String>
) : RuntimeException("Requested permissions have been denied")