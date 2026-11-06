package pe.greenminds.ecomind.iam.application

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.greenminds.ecomind.iam.domain.model.PendingRegistration
import pe.greenminds.ecomind.iam.domain.model.Session
import pe.greenminds.ecomind.iam.domain.model.SocialRole
import pe.greenminds.ecomind.iam.domain.repositories.AuthRepository
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import java.io.IOException

class AccountAccessUseCasesTest {

    private class FakeAuthRepository : AuthRepository {
        var onRequestRecovery: suspend (String) -> Result<Unit> = { error("Not used") }
        var onConfirmRecovery: suspend (String, String) -> Result<Unit> = { _, _ -> error("Not used") }
        var onLogout: suspend () -> Result<Unit> = { error("Not used") }

        override suspend fun requestPasswordRecovery(email: String) = onRequestRecovery(email)
        override suspend fun confirmPasswordRecovery(token: String, newPassword: String) =
            onConfirmRecovery(token, newPassword)
        override suspend fun logout() = onLogout()
        override suspend fun signIn(email: String, password: String): Result<Session> = error("Not used")
        override suspend fun signUp(
            name: String, email: String, password: String, role: SocialRole
        ): Result<PendingRegistration> = error("Not used")
        override suspend fun verifyEmail(email: String, code: String): Result<Unit> = error("Not used")
    }

    private class FakeSessionRepository : SessionRepository {
        var session: Session? = Session(1L, "user@example.com", "test-token", Long.MAX_VALUE)
        var clearFailure: Exception? = null

        override fun getSession(): Flow<Session?> = flowOf(session)
        override suspend fun saveSession(session: Session) { this.session = session }
        override suspend fun clearSession() {
            yield()
            clearFailure?.let { throw it }
            session = null
        }
    }

    @Test
    fun recoveryNormalizesTheAccountEmail() = runBlocking {
        val auth = FakeAuthRepository().apply {
            onRequestRecovery = { email ->
                assertEquals("user@example.com", email)
                Result.success(Unit)
            }
        }

        assertTrue(RequestPasswordRecoveryUseCase(auth)(" User@Example.com ").isSuccess)
    }

    @Test
    fun confirmationTrimsTheTokenWithoutChangingTheNewPassword() = runBlocking {
        val auth = FakeAuthRepository().apply {
            onConfirmRecovery = { token, password ->
                assertEquals("recovery-token", token)
                assertEquals(" GreenPlanet2026 ", password)
                Result.success(Unit)
            }
        }

        assertTrue(ConfirmPasswordRecoveryUseCase(auth)(" recovery-token ", " GreenPlanet2026 ").isSuccess)
    }

    @Test
    fun logoutAuthenticatesBeforeClearingTheLocalSession() = runBlocking {
        val sessions = FakeSessionRepository()
        val auth = FakeAuthRepository().apply {
            onLogout = {
                assertEquals("test-token", sessions.session?.accessToken)
                Result.success(Unit)
            }
        }

        assertTrue(LogoutUseCase(auth, sessions)().isSuccess)
        assertNull(sessions.session)
    }

    @Test
    fun logoutClearsTheLocalSessionWhenTheServerCannotBeReached() = runBlocking {
        val failure = IOException("Connection unavailable")
        val sessions = FakeSessionRepository()
        val auth = FakeAuthRepository().apply { onLogout = { Result.failure(failure) } }

        val result = LogoutUseCase(auth, sessions)()

        assertSame(failure, result.exceptionOrNull())
        assertNull(sessions.session)
    }

    @Test
    fun cancelledLogoutStillCompletesTheSuspendingSessionCleanup() = runBlocking {
        val started = CompletableDeferred<Unit>()
        val sessions = FakeSessionRepository()
        val auth = FakeAuthRepository().apply {
            onLogout = {
                started.complete(Unit)
                awaitCancellation()
            }
        }
        val job = launch { LogoutUseCase(auth, sessions)() }
        started.await()

        job.cancelAndJoin()

        assertTrue(job.isCancelled)
        assertNull(sessions.session)
    }

    @Test
    fun aSessionStorageFailureDoesNotReportSuccessfulLogout() {
        val failure = IOException("Session storage unavailable")
        val sessions = FakeSessionRepository().apply { clearFailure = failure }
        val auth = FakeAuthRepository().apply { onLogout = { Result.success(Unit) } }

        try {
            runBlocking { LogoutUseCase(auth, sessions)() }
            throw AssertionError("Expected session storage failure")
        } catch (exception: IOException) {
            assertEquals(failure.message, exception.message)
            assertTrue(sessions.session != null)
        }
    }
}
