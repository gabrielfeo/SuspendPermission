package com.gabrielfeo.suspend.permission.assurer

import android.content.Context
import androidx.core.content.PermissionChecker
import com.gabrielfeo.suspend.permission.assurer.PermissionAssurerImpl.PermissionResults
import com.gabrielfeo.suspend.permission.assurer.PermissionAssurerImpl.PermissionResults.CURRENTLY_DENIED
import com.gabrielfeo.suspend.permission.assurer.PermissionAssurerImpl.PermissionResults.GRANTED
import com.gabrielfeo.suspend.permission.assurer.PermissionAssurerImpl.PermissionResults.PERMANENTLY_DENIED
import kotlinx.coroutines.*

/**
 * [PermissionAssurerImpl] with standard arguments for Android. Still requires arguments for things specific
 * to the calling component, such as [isPermanentlyDenied] which is usually
 * [android.app.Fragment.shouldShowRequestPermissionRationale] or
 * [android.app.Activity.shouldShowRequestPermissionRationale].
 */
@Suppress("FunctionName")
internal fun AndroidPermissionAssurer(
    context: Context,
    isPermanentlyDenied: (permission: String) -> Boolean,
    dispatcher: CoroutineDispatcher = Dispatchers.Main // TODO Could be background?
): PermissionAssurer = PermissionAssurerImpl(
    dispatcher = dispatcher,
    getPermissionResult = { permission -> PermissionChecker.checkCallingOrSelfPermission(context, permission) },
    mapResult = { permission: String, result: Int ->
        when {
            result == PermissionChecker.PERMISSION_GRANTED -> GRANTED
            isPermanentlyDenied(permission) -> PERMANENTLY_DENIED
            else -> CURRENTLY_DENIED
        }
    }
)


/**
 * Testable implementation of `PermissionAssurer`.
 *
 * @property getPermissionResult Gets the current result of a permission request. Must not trigger a request to the
 * user.
 * @param mapResult Maps the `platformResult` to one of the constants [PermissionResults].
 * @param dispatcher The `CoroutineDispatcher` with which coroutines to get the current permission result should be
 * started.
 */
internal open class PermissionAssurerImpl internal constructor(
    private val getPermissionResult: (permission: String) -> Int,
    private val mapResult: (permission: String, platformResult: Int) -> Int,
    private val dispatcher: CoroutineDispatcher
) : PermissionAssurer {

    internal companion object PermissionResults {
        const val GRANTED = 0
        const val CURRENTLY_DENIED = 1
        const val PERMANENTLY_DENIED = 2
    }

    override suspend fun ensureGranted(
        scope: CoroutineScope,
        permissions: Array<out String>
    ) {
        val resultsByPermission = permissions.associate { permission ->
            permission to scope.async(dispatcher) { getPermissionResult(permission) }
        }
        ensureAllGranted(resultsByPermission)
    }

    override suspend fun ensureGrantedInResults(
        scope: CoroutineScope,
        grantResults: IntArray,
        requestedPermissions: Array<out String>
    ) {
        val resultsByPermission: MutableMap<String, Deferred<Int>> = HashMap()
        requestedPermissions.forEachIndexed { i, permission ->
            resultsByPermission[permission] = scope.async(dispatcher) { mapResult(permission, grantResults[i]) }
        }
        ensureAllGranted(resultsByPermission)
    }

    private suspend fun ensureAllGranted(resultsByPermission: Map<String, Deferred<Int>>) {
        val permanentlyDeniedPermissions = ArrayList<String>()
        val currentlyDeniedPermissions = ArrayList<String>()
        val deniedPermissions = ArrayList<String>()
        resultsByPermission.forEach { (permission, asyncResult) ->
            when (mapResult(permission, asyncResult.await())) {
                CURRENTLY_DENIED -> {
                    deniedPermissions.add(permission)
                    currentlyDeniedPermissions.add(permission)
                }
                PERMANENTLY_DENIED -> {
                    deniedPermissions.add(permission)
                    permanentlyDeniedPermissions.add(permission)
                }
            }
        }
        if (deniedPermissions.isNotEmpty()) {
            throw PermissionsDeniedException(
                deniedPermissions.toTypedArray(),
                permanentlyDeniedPermissions.toTypedArray(), // TODO Review
                currentlyDeniedPermissions.toTypedArray()
            )
        }
    }

}