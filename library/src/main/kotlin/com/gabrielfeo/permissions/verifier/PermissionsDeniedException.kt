package com.gabrielfeo.permissions.verifier

class PermissionsDeniedException(
    val deniedPermissions: Array<out String>,
    val permanentlyDeniedPermissions: Array<out String>,
    val currentlyDeniedPermissions: Array<out String>
) : RuntimeException("Requested permissions have been denied")