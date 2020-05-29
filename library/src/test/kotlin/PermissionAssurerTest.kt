import com.gabrielfeo.permissions.verifier.PermissionAssurer
import com.gabrielfeo.permissions.verifier.PermissionAssurerImpl
import com.gabrielfeo.permissions.verifier.PermissionAssurerImpl.PermissionResults.CURRENTLY_DENIED
import com.gabrielfeo.permissions.verifier.PermissionAssurerImpl.PermissionResults.GRANTED
import com.gabrielfeo.permissions.verifier.PermissionAssurerImpl.PermissionResults.PERMANENTLY_DENIED
import com.gabrielfeo.permissions.verifier.PermissionsDeniedException.PermissionsCurrentlyDeniedException
import com.gabrielfeo.permissions.verifier.PermissionsDeniedException.PermissionsPermanentlyDeniedException
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import kotlin.test.Test
import kotlin.test.assertFalse
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
        } catch (exception: PermissionsCurrentlyDeniedException) {
            val denied = exception.deniedPermissions
            assertTrue(denied.size == 1 && denied.first() == "B")
            assertFalse(exception.allRequestedAreDenied)
        }
    }

    @Test
    fun `Requesting denied permissions specifies when all are denied`() = runBlockingTest {
        val verifier = createVerifier(permissionsToGrant = emptyArray())
        try {
            verifier.ensureGranted(this, permissions = arrayOf("B", "C"))
            assert(false) { "Expected exception not thrown" }
        } catch (exception: PermissionsCurrentlyDeniedException) {
            assertTrue(exception.allRequestedAreDenied)
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
        } catch (exception: PermissionsPermanentlyDeniedException) {
            assertFalse(exception.allRequestedAreDenied)
            assertFalse(exception.allRequestedArePermanentlyDenied)
            assertTrue(exception.deniedPermissions.contentEquals(arrayOf("B", "C")))
            assertTrue(exception.currentlyDeniedPermissions.contentEquals(arrayOf("B")))
            assertTrue(exception.permanentlyDeniedPermissions.contentEquals(arrayOf("C")))
        }
    }

    @Test
    fun `Requesting permanently denied permissions specified when all are permanently denied`() = runBlockingTest {
        val toBePermanentlyDenied =  arrayOf("C")
        val verifier = createVerifier(permissionsToPermanentlyDeny = toBePermanentlyDenied)
        try {
            verifier.ensureGranted(this, toBePermanentlyDenied)
            assert(false) { "Expected exception not thrown" }
        } catch (exception: PermissionsPermanentlyDeniedException) {
            assertTrue(exception.allRequestedAreDenied)
            assertTrue(exception.allRequestedArePermanentlyDenied)
        }
    }

}