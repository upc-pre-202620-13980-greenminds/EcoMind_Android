package pe.greenminds.ecomind.iam.infrastructure.local

import kotlinx.coroutines.delay
import pe.greenminds.ecomind.iam.domain.model.AuthError
import pe.greenminds.ecomind.iam.domain.model.AuthException
import pe.greenminds.ecomind.iam.domain.model.Session
import pe.greenminds.ecomind.iam.domain.repositories.AuthRepository
import javax.inject.Inject

// Demo implementation used until the web services are deployed
class LocalAuthRepository @Inject constructor() : AuthRepository {

    private data class DemoAccount(val id: Long, val email: String, val password: String)

    companion object {
        // Fictional account taken from the examples of the web services documentation
        private val DEMO_ACCOUNT = DemoAccount(
            id = 1L,
            email = "camila.torres@example.com",
            password = "GreenPlanet2026"
        )

        private const val DEMO_TOKEN = "demo-access-token"
        private const val SIMULATED_DELAY_MILLIS = 800L
        private const val SESSION_DURATION_MILLIS = 60 * 60 * 1000L
    }

    override suspend fun signIn(email: String, password: String): Result<Session> {
        // Simulates the time a request to the web services would take
        delay(SIMULATED_DELAY_MILLIS)

        // The answer is the same whether the email or the password is wrong
        if (email != DEMO_ACCOUNT.email || password != DEMO_ACCOUNT.password) {
            return Result.failure(AuthException(AuthError.INVALID_CREDENTIALS))
        }

        val session = Session(
            accountId = DEMO_ACCOUNT.id,
            email = DEMO_ACCOUNT.email,
            accessToken = DEMO_TOKEN,
            expiresAtMillis = System.currentTimeMillis() + SESSION_DURATION_MILLIS
        )
        return Result.success(session)
    }
}
