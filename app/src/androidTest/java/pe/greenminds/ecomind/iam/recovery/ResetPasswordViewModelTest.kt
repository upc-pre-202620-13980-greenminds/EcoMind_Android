package pe.greenminds.ecomind.iam.recovery

import androidx.lifecycle.viewModelScope
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.iam.application.ConfirmPasswordRecoveryUseCase
import pe.greenminds.ecomind.iam.domain.model.PendingRegistration
import pe.greenminds.ecomind.iam.domain.model.Session
import pe.greenminds.ecomind.iam.domain.model.SocialRole
import pe.greenminds.ecomind.iam.domain.repositories.AuthRepository
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.iam.interfaces.recovery.ResetPasswordViewModel
import java.io.IOException

class ResetPasswordViewModelTest {
    private class FakeAuthRepository : AuthRepository {
        val started = CompletableDeferred<Unit>()
        val response = CompletableDeferred<Result<Unit>>()
        var confirmationCalls = 0

        override suspend fun confirmPasswordRecovery(token: String, newPassword: String): Result<Unit> {
            confirmationCalls++
            started.complete(Unit)
            return response.await()
        }

        override suspend fun signIn(email: String, password: String): Result<Session> = error("Not used")
        override suspend fun signUp(
            name: String, email: String, password: String, role: SocialRole
        ): Result<PendingRegistration> = error("Not used")
        override suspend fun verifyEmail(email: String, code: String): Result<Unit> = error("Not used")
        override suspend fun requestPasswordRecovery(email: String): Result<Unit> = error("Not used")
        override suspend fun logout(): Result<Unit> = error("Not used")
    }

    private class FakeSessionRepository : SessionRepository {
        var session: Session? = Session(1L, "user@example.com", "previous-session", Long.MAX_VALUE)
        var failClear = false
        var clearCalls = 0
        val cleared = CompletableDeferred<Unit>()

        override fun getSession(): Flow<Session?> = flowOf(session)
        override suspend fun saveSession(session: Session) { this.session = session }
        override suspend fun clearSession() {
            clearCalls++
            yield()
            if (failClear) throw IOException("Session storage unavailable")
            session = null
            cleared.complete(Unit)
        }
    }

    @Test
    fun aNewLinkDoesNotPreventCleanupOrReceiveThePreviousResult() = runBlocking {
        val auth = FakeAuthRepository()
        val sessions = FakeSessionRepository()
        val viewModel = onMain { ResetPasswordViewModel(ConfirmPasswordRecoveryUseCase(auth), sessions) }
        try {
            onMain { submit(viewModel, "first-link") }
            withTimeout(5_000) { auth.started.await() }
            onMain {
                viewModel.setToken("second-link")
                viewModel.onPasswordChange("SecondPassword2026")
                viewModel.onConfirmPasswordChange("SecondPassword2026")
            }
            auth.response.complete(Result.success(Unit))
            withTimeout(5_000) { sessions.cleared.await() }

            onMain {
                assertNull(sessions.session)
                assertEquals(1, sessions.clearCalls)
                assertFalse(viewModel.state.value.isCompleted)
                assertFalse(viewModel.state.value.isLoading)
                assertEquals("SecondPassword2026", viewModel.state.value.password)
                assertEquals("SecondPassword2026", viewModel.state.value.confirmPassword)
            }
        } finally {
            auth.response.complete(Result.success(Unit))
            onMain { viewModel.viewModelScope.cancel() }
        }
    }

    @Test
    fun failedCleanupRetriesStorageWithoutConsumingTheResetTokenAgain() = runBlocking {
        val auth = FakeAuthRepository()
        val sessions = FakeSessionRepository().apply { failClear = true }
        val viewModel = onMain { ResetPasswordViewModel(ConfirmPasswordRecoveryUseCase(auth), sessions) }
        try {
            onMain { submit(viewModel, "one-use-link") }
            withTimeout(5_000) { auth.started.await() }
            auth.response.complete(Result.success(Unit))
            val completed = withTimeout(5_000) {
                viewModel.state.first { it.isCompleted && !it.isLoading }
            }
            assertEquals(R.string.recovery_session_clear_error, completed.formError)
            assertEquals("", completed.password)
            assertEquals("", completed.confirmPassword)
            assertFalse(completed.isFinished)
            onMain {
                assertNotNull(sessions.session)
                viewModel.resetPassword()
                sessions.failClear = false
                viewModel.finish()
            }
            withTimeout(5_000) { viewModel.state.first { it.isFinished } }
            onMain {
                assertEquals(1, auth.confirmationCalls)
                assertEquals(2, sessions.clearCalls)
                assertNull(sessions.session)
                assertNull(viewModel.state.value.formError)
            }
        } finally {
            auth.response.complete(Result.success(Unit))
            onMain { viewModel.viewModelScope.cancel() }
        }
    }

    @Test
    fun leavingTheScreenDuringConfirmationStillClearsASuccessfulResetSession() = runBlocking {
        val auth = FakeAuthRepository()
        val sessions = FakeSessionRepository()
        val viewModel = onMain { ResetPasswordViewModel(ConfirmPasswordRecoveryUseCase(auth), sessions) }
        try {
            onMain { submit(viewModel, "pending-link") }
            withTimeout(5_000) { auth.started.await() }
            onMain { viewModel.viewModelScope.cancel() }
            auth.response.complete(Result.success(Unit))
            withTimeout(5_000) { sessions.cleared.await() }
            onMain {
                assertEquals(1, auth.confirmationCalls)
                assertEquals(1, sessions.clearCalls)
                assertNull(sessions.session)
            }
        } finally {
            auth.response.complete(Result.success(Unit))
            onMain { viewModel.viewModelScope.cancel() }
        }
    }

    private fun submit(viewModel: ResetPasswordViewModel, token: String) {
        viewModel.setToken(token)
        viewModel.onPasswordChange("GreenPlanet2026")
        viewModel.onConfirmPasswordChange("GreenPlanet2026")
        viewModel.resetPassword()
        assertTrue(viewModel.state.value.isLoading)
    }

    private fun <T> onMain(block: () -> T): T {
        var result: Result<T>? = null
        InstrumentationRegistry.getInstrumentation().runOnMainSync { result = runCatching(block) }
        return checkNotNull(result).getOrThrow()
    }
}
