package pe.greenminds.ecomind.iam.infrastructure.implementation

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.greenminds.ecomind.iam.domain.model.AuthError
import pe.greenminds.ecomind.iam.domain.model.AuthException
import pe.greenminds.ecomind.iam.infrastructure.remote.*
import retrofit2.Response
import java.time.Instant

class RemoteAuthRepositoryTest {
    private class FakeAuthApi(
        val onSignIn: suspend (SignInRequest) -> Response<SignInResponse> = { error("Not used") }
    ) : AuthApi {
        var onRequestRecovery: suspend (PasswordRecoveryRequest) -> Response<PasswordRecoveryResponse> =
            { error("Not used") }
        var onConfirmRecovery: suspend (ConfirmPasswordRecoveryRequest) -> Response<Unit> =
            { error("Not used") }
        var onLogout: suspend () -> Response<Unit> = { error("Not used") }

        override suspend fun signIn(request: SignInRequest) = onSignIn(request)
        override suspend fun signUp(request: SignUpRequest): Response<SignUpResponse> =
            error("Not used by these login tests")
        override suspend fun verifyEmail(request: VerifyEmailRequest): Response<VerifyEmailResponse> =
            error("Not used by these login tests")
        override suspend fun requestPasswordRecovery(request: PasswordRecoveryRequest) = onRequestRecovery(request)
        override suspend fun confirmPasswordRecovery(request: ConfirmPasswordRecoveryRequest) = onConfirmRecovery(request)
        override suspend fun logout() = onLogout()
    }

    @Test
    fun signInConvertsTheBackendExpirationAndPassesCredentials() = runBlocking {
        val expiration = "2026-10-09T16:00:00.123456Z"
        val repository = RemoteAuthRepository(FakeAuthApi { request ->
            assertEquals(SignInRequest("user@example.com", "password"), request)
            Response.success(SignInResponse("test-jwt", expiration, 42L, request.email))
        })

        val session = repository.signIn("user@example.com", "password").getOrThrow()

        assertEquals(42L, session.accountId)
        assertEquals("test-jwt", session.accessToken)
        assertEquals(Instant.parse(expiration).toEpochMilli(), session.expiresAtMillis)
    }

    @Test
    fun invalidCredentialsMapToTheErrorUsedByTheScreen() = runBlocking {
        val repository = RemoteAuthRepository(FakeAuthApi {
            Response.error<SignInResponse>(401,
                """{"code":"INVALID_CREDENTIALS","message":"Invalid credentials"}"""
                    .toResponseBody("application/json".toMediaType()))
        })

        val exception = repository.signIn("user@example.com", "wrong").exceptionOrNull()

        assertEquals(AuthError.INVALID_CREDENTIALS, (exception as AuthException).error)
    }

    @Test
    fun cancellationPropagatesInsteadOfBecomingALoginFailure() {
        val cancellation = CancellationException("Screen closed")
        val repository = RemoteAuthRepository(FakeAuthApi { throw cancellation })

        try {
            runBlocking { repository.signIn("user@example.com", "password") }
            throw AssertionError("Expected coroutine cancellation")
        } catch (exception: CancellationException) {
            assertSame(cancellation, exception)
        }
    }

    @Test
    fun recoveryRequestPassesTheEmailAndAcceptsTheGenericResponse() = runBlocking {
        val api = FakeAuthApi().apply {
            onRequestRecovery = { request ->
                assertEquals(PasswordRecoveryRequest("user@example.com"), request)
                Response.success(PasswordRecoveryResponse("If the account exists, a link has been sent."))
            }
        }

        assertTrue(RemoteAuthRepository(api).requestPasswordRecovery("user@example.com").isSuccess)
    }

    @Test
    fun passwordConfirmationAcceptsAnEmpty204Response() = runBlocking {
        val api = FakeAuthApi().apply {
            onConfirmRecovery = { request ->
                assertEquals(ConfirmPasswordRecoveryRequest("recovery-token", "GreenPlanet2026"), request)
                Response.success<Unit>(204, null)
            }
        }

        assertTrue(RemoteAuthRepository(api).confirmPasswordRecovery("recovery-token", "GreenPlanet2026").isSuccess)
    }

    @Test
    fun logoutAcceptsAnEmpty204Response() = runBlocking {
        val api = FakeAuthApi().apply { onLogout = { Response.success<Unit>(204, null) } }

        assertTrue(RemoteAuthRepository(api).logout().isSuccess)
    }

    @Test
    fun invalidRecoveryTokenMapsToTheRecoveryError() = runBlocking {
        val api = FakeAuthApi().apply {
            onConfirmRecovery = {
                Response.error(422,
                    """{"code":"PASSWORD_RESET_TOKEN_INVALID","message":"The recovery link is invalid."}"""
                        .toResponseBody("application/json".toMediaType()))
            }
        }

        val exception = RemoteAuthRepository(api).confirmPasswordRecovery("used-token", "GreenPlanet2026")
            .exceptionOrNull()

        assertEquals(AuthError.PASSWORD_RESET_TOKEN_INVALID, (exception as AuthException).error)
    }

    @Test
    fun weakPasswordMapsToThePasswordPolicyError() = runBlocking {
        val api = FakeAuthApi().apply {
            onConfirmRecovery = {
                Response.error(400,
                    """{"code":"PASSWORD_POLICY_VIOLATION","message":"The password is too weak."}"""
                        .toResponseBody("application/json".toMediaType()))
            }
        }

        val exception = RemoteAuthRepository(api).confirmPasswordRecovery("recovery-token", "weak")
            .exceptionOrNull()

        assertEquals(AuthError.PASSWORD_POLICY_VIOLATION, (exception as AuthException).error)
    }

    @Test
    fun failedLogoutKeepsTheNetworkFailure() = runBlocking {
        val failure = java.io.IOException("Connection unavailable")
        val api = FakeAuthApi().apply { onLogout = { throw failure } }

        assertSame(failure, RemoteAuthRepository(api).logout().exceptionOrNull())
    }

    @Test
    fun recoveryCancellationPropagates() {
        val cancellation = CancellationException("Screen closed")
        val api = FakeAuthApi().apply { onConfirmRecovery = { throw cancellation } }

        try {
            runBlocking { RemoteAuthRepository(api).confirmPasswordRecovery("recovery-token", "GreenPlanet2026") }
            throw AssertionError("Expected coroutine cancellation")
        } catch (exception: CancellationException) {
            assertSame(cancellation, exception)
        }
    }
}
