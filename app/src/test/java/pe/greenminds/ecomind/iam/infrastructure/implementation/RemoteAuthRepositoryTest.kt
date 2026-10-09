package pe.greenminds.ecomind.iam.infrastructure.implementation

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test
import pe.greenminds.ecomind.iam.domain.model.AuthError
import pe.greenminds.ecomind.iam.domain.model.AuthException
import pe.greenminds.ecomind.iam.infrastructure.remote.*
import retrofit2.Response
import java.time.Instant

class RemoteAuthRepositoryTest {
    private class FakeAuthApi(
        val onSignIn: suspend (SignInRequest) -> Response<SignInResponse>
    ) : AuthApi {
        override suspend fun signIn(request: SignInRequest) = onSignIn(request)
        override suspend fun signUp(request: SignUpRequest): Response<SignUpResponse> =
            error("Not used by these login tests")
        override suspend fun verifyEmail(request: VerifyEmailRequest): Response<VerifyEmailResponse> =
            error("Not used by these login tests")
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
}
