package com.gabrielfeo.permissions.verifier

sealed class PermissionsDeniedException(
    val deniedPermissions: Array<out String>,
    val allRequestedAreDenied: Boolean
) : RuntimeException("Requested permissions have been denied") {

    class PermissionsCurrentlyDeniedException(
        deniedPermissions: Array<out String>,
        allRequestedAreDenied: Boolean
    ) : PermissionsDeniedException(deniedPermissions, allRequestedAreDenied)

    // TODO Should be no overlap between permanently denied and currently denied arrays
    class PermissionsPermanentlyDeniedException(
        val permanentlyDeniedPermissions: Array<out String>,
        deniedPermissions: Array<out String>,
        allRequestedAreDenied: Boolean
    ) : PermissionsDeniedException(deniedPermissions, allRequestedAreDenied) {

        val allRequestedArePermanentlyDenied
            get() = permanentlyDeniedPermissions.size == deniedPermissions.size

    }

}