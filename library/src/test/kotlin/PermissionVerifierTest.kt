import com.gabrielfeo.permissions.verifier.PermissionVerifier
import com.gabrielfeo.permissions.verifier.PermissionVerifierImpl
import com.gabrielfeo.permissions.verifier.PermissionsDeniedException.PermissionsCurrentlyDeniedException
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PermissionVerifierTest {

    private fun createVerifier(permissionsToGrant: Array<out String>): PermissionVerifier {
        return PermissionVerifierImpl(
            getPermissionResult = { permission -> if (permission in permissionsToGrant) 1 else 0 },
            isResultGrantedResult = { result -> result == 1 },
            dispatcher = TestCoroutineDispatcher()
        )
    }

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

}