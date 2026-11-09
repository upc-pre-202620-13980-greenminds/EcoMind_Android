package pe.greenminds.ecomind.iam

import androidx.lifecycle.viewModelScope
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.cancel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.iam.application.SignUpUseCase
import pe.greenminds.ecomind.iam.application.VerifyEmailUseCase
import pe.greenminds.ecomind.iam.domain.model.PendingRegistration
import pe.greenminds.ecomind.iam.domain.model.Session
import pe.greenminds.ecomind.iam.domain.model.SocialRole
import pe.greenminds.ecomind.iam.domain.repositories.AuthRepository
import pe.greenminds.ecomind.iam.interfaces.signup.SignUpStep
import pe.greenminds.ecomind.iam.interfaces.signup.SignUpViewModel

class SignUpTermsViewModelTest {
    @Test
    fun registrationRequiresExplicitTermsAcceptance() = onMain {
        val repository = RecordingAuthRepository()
        val viewModel = validForm(repository)
        try {
            assertFalse(viewModel.state.value.termsAccepted)
            viewModel.submitRegistration()

            val rejected = viewModel.state.value
            assertEquals(R.string.error_terms_required, rejected.termsError)
            assertNull(rejected.nameError)
            assertNull(rejected.emailError)
            assertNull(rejected.roleError)
            assertNull(rejected.passwordError)
            assertNull(rejected.confirmPasswordError)
            assertEquals(SignUpStep.FORM, rejected.step)
            assertEquals(0, repository.registrationCalls)

            viewModel.onTermsAcceptedChange(true)
            viewModel.submitRegistration()

            assertNull(viewModel.state.value.termsError)
            assertEquals(SignUpStep.CODE, viewModel.state.value.step)
            assertEquals(1, repository.registrationCalls)
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }

    @Test
    fun withdrawingAcceptanceBeforeResubmittingBlocksRegistration() = onMain {
        val repository = RecordingAuthRepository()
        val viewModel = validForm(repository)
        try {
            viewModel.onTermsAcceptedChange(true)
            viewModel.submitRegistration()
            assertEquals(1, repository.registrationCalls)

            viewModel.backToForm()
            viewModel.onTermsAcceptedChange(false)
            viewModel.submitRegistration()

            assertEquals(R.string.error_terms_required, viewModel.state.value.termsError)
            assertEquals(SignUpStep.FORM, viewModel.state.value.step)
            assertEquals(1, repository.registrationCalls)
        } finally {
            viewModel.viewModelScope.cancel()
        }
    }

    private fun validForm(repository: AuthRepository) =
        SignUpViewModel(SignUpUseCase(repository), VerifyEmailUseCase(repository)).apply {
            onNameChange("Test Student")
            onEmailChange("student@example.invalid")
            onRoleSelected(SocialRole.STUDENT)
            onPasswordChange("GreenPlanet2026")
            onConfirmPasswordChange("GreenPlanet2026")
        }

    private class RecordingAuthRepository : AuthRepository {
        var registrationCalls = 0

        override suspend fun signUp(
            name: String, email: String, password: String, role: SocialRole
        ): Result<PendingRegistration> {
            registrationCalls++
            return Result.success(PendingRegistration(email, Long.MAX_VALUE))
        }

        override suspend fun signIn(email: String, password: String): Result<Session> = error("Not used")
        override suspend fun verifyEmail(email: String, code: String): Result<Unit> = error("Not used")
        override suspend fun requestPasswordRecovery(email: String): Result<Unit> = error("Not used")
        override suspend fun confirmPasswordRecovery(token: String, newPassword: String): Result<Unit> = error("Not used")
        override suspend fun logout(): Result<Unit> = error("Not used")
    }

    private fun onMain(block: () -> Unit) {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(block)
    }
}
