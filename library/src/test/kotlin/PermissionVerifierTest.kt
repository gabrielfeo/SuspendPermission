import com.gabrielfeo.permissions.verifier.PermissionVerifier
import com.gabrielfeo.permissions.verifier.PermissionVerifierImpl
import com.gabrielfeo.permissions.verifier.PermissionsDeniedException.PermissionsCurrentlyDeniedException
import com.gabrielfeo.permissions.verifier.ensureGranted
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import kotlin.test.Test

class PermissionVerifierTest {

    private fun createVerifier(grantingPermissions: Boolean): PermissionVerifier =
        PermissionVerifierImpl(
            getPermissionResult = { 0 },
            isResultGrantedResult = { grantingPermissions },
            dispatcher = TestCoroutineDispatcher()
        )

    @Test
    fun `Requesting granted permissions resumes execution`() = runBlockingTest {
        val verifier = createVerifier(grantingPermissions = true)
        verifier.ensureGranted(this, "A", "B", "C")
    }

    @Test
    fun `Requesting denied permissions throws`() = runBlockingTest {
        val verifier = createVerifier(grantingPermissions = false)
        try {
            verifier.ensureGranted(this, "A", "B", "C")
            assert(false) // should've thrown
        } catch (_: PermissionsCurrentlyDeniedException) {
            assert(true)
        }
    }

}