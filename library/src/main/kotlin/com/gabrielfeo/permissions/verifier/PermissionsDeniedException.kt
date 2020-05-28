package com.gabrielfeo.permissions.verifier

sealed class PermissionsDeniedException(
    val deniedPermissions: Array<out String>,
    val allPermissionsAreDenied: Boolean
) : RuntimeException("Requested permissions have been denied") {

    class PermissionsCurrentlyDeniedException(
        deniedPermissions: Array<out String>, allPermissionsWereDenied: Boolean
    ) : PermissionsDeniedException(deniedPermissions, allPermissionsWereDenied)

    // TODO Should be no overlap between permanently denied and currently denied arrays
    class PermissionsPermanentlyDeniedException(
        val permanentlyDeniedPermissions: Array<out String>,
        deniedPermissions: Array<out String>, allPermissionsWereDenied: Boolean
    ) : PermissionsDeniedException(deniedPermissions, allPermissionsWereDenied) {
        val allPermissionsArePermanentlyDenied get() = permanentlyDeniedPermissions.size == deniedPermissions.size
    }

}