package com.gabrielfeo.meetingpoint.app.android.infrastructure.permission

import android.Manifest.permission.*
import android.os.Build

object UsedPermissions {

    val location = when {
        API_LEVEL >= 28 -> arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION, FOREGROUND_SERVICE)
        API_LEVEL >= 29 -> arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION, FOREGROUND_SERVICE, ACCESS_BACKGROUND_LOCATION)
        else -> arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION)
    }

    fun areMainLocationPermissions(permissions: Array<out String>) = permissions.all { permission ->
        permission == ACCESS_COARSE_LOCATION || permission == ACCESS_FINE_LOCATION
    }

    fun areBackgroundLocationPermissions(permissions: Array<out String>) =
        API_LEVEL >= 28
            && permissions.all { permission ->
                (API_LEVEL >= 28 && permission == FOREGROUND_SERVICE)
                    || (API_LEVEL >= 29 && permission == ACCESS_BACKGROUND_LOCATION)
            }

    private val API_LEVEL get() = Build.VERSION.SDK_INT

}