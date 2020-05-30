import com.gabrielfeo.suspend.permission.assurer.PermissionAssurer
import com.gabrielfeo.suspend.permission.assurer.PermissionAssurerImpl
import com.gabrielfeo.suspend.permission.assurer.PermissionAssurerImpl.PermissionResults.CURRENTLY_DENIED
import com.gabrielfeo.suspend.permission.assurer.PermissionAssurerImpl.PermissionResults.GRANTED
import com.gabrielfeo.suspend.permission.assurer.PermissionAssurerImpl.PermissionResults.PERMANENTLY_DENIED
import com.gabrielfeo.suspend.permission.assurer.PermissionsDeniedException
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import kotlin.test.Test
import kotlin.test.assertTrue

class PermissionAssurerTest {

    private fun createVerifier(
        permissionsToGrant: Array<out String> = emptyArray(),
        permissionsToPermanentlyDeny: Array<out String> = emptyArray()
    ): PermissionAssurer = PermissionAssurerImpl(
        dispatcher = TestCoroutineDispatcher(),
        mapResult = { _, result -> result },
        getPermissionResult = { permission ->
            when (permission) {
                in permissionsToGrant -> GRANTED
                in permissionsToPermanentlyDeny -> PERMANENTLY_DENIED
                else -> CURRENTLY_DENIED
            }
        }
    )

    @Test
    fun `Requesting granted permissions resumes execution`() = runBlockingTest {
        val permissionsToGrant =  arrayOf("A", "B", "C")
        val verifier = createVerifier(permissionsToGrant)
        verifier.ensureGranted(this, permissions = permissionsToGrant)
    }

    @Test
    fun `Requesting denied permissions throws`() = runBlockingTest {
        val permissionsToGrant =  arrayOf("A")
        val verifier = createVerifier(permissionsToGrant)
        try {
            verifier.ensureGranted(this, permissions = permissionsToGrant + "B")
            assert(false) { "Expected exception not thrown" }
        } catch (exception: PermissionsDeniedException) {
            with(exception) {
                assertTrue(permanentlyDeniedPermissions.isEmpty())
                assertTrue(deniedPermissions.contentEquals(currentlyDeniedPermissions))
                with(deniedPermissions) {
                    assertTrue(size == 1 && first() == "B")
                }
            }
        }
    }

    @Test
    fun `Requesting permanently denied permissions throws`() = runBlockingTest {
        val toBeGranted =  arrayOf("A")
        val toBeDenied =  arrayOf("B")
        val toBePermanentlyDenied =  arrayOf("C")
        val verifier = createVerifier(toBeGranted, toBePermanentlyDenied)
        try {
            val permissionsToRequest = toBeGranted + toBeDenied + toBePermanentlyDenied
            verifier.ensureGranted(this, permissionsToRequest)
            assert(false) { "Expected exception not thrown" }
        } catch (exception: PermissionsDeniedException) {
            assertTrue(exception.deniedPermissions.contentEquals(arrayOf("B", "C")))
            assertTrue(exception.currentlyDeniedPermissions.contentEquals(arrayOf("B")))
            assertTrue(exception.permanentlyDeniedPermissions.contentEquals(arrayOf("C")))
        }
    }

}