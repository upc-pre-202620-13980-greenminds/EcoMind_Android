package pe.greenminds.ecomind.iam.infrastructure.remote

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import java.io.IOException
import org.junit.Test
import pe.greenminds.ecomind.BuildConfig
import pe.greenminds.ecomind.iam.domain.model.Session
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository

class AuthInterceptorTest {
    private fun headerFor(path: String, expiresAt: Long, foreign: Boolean = false,
        capturedSession: Session? = null): String? {
        val sessions = object : SessionRepository {
            override fun getSession(): Flow<Session?> = flowOf(
                Session(1L, "test@example.com", "test-token", expiresAt)
            )
            override suspend fun saveSession(session: Session) = Unit
            override suspend fun clearSession() = Unit
        }
        var captured: Request? = null
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(sessions))
            .addInterceptor { chain ->
                captured = chain.request()
                Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                    .code(200).message("OK").body("".toResponseBody()).build()
            }.build()
        val url = if (foreign) "https://other.example/api/v1/$path" else BuildConfig.API_BASE_URL + path
        val request = Request.Builder().url(url).apply {
            capturedSession?.let { tag(Session::class.java, it) }
        }.build()
        client.newCall(request).execute().use { }
        return captured?.header("Authorization")
    }

    @Test fun addsValidSessionToken() {
        assertEquals("Bearer test-token", headerFor("quests", Long.MAX_VALUE))
    }
    @Test fun skipsPublicLogin() {
        assertNull(headerFor("authentication/sign-in", Long.MAX_VALUE))
    }
    @Test fun skipsExpiredSession() {
        assertNull(headerFor("quests", 0L))
    }
    @Test fun neverAddsTokenToAnotherServer() {
        assertNull(headerFor("quests", Long.MAX_VALUE, foreign = true))
    }
    @Test fun authorizesTaggedGamificationThroughTheSameInterceptor() {
        assertEquals("Bearer test-token", headerFor("gamification/me/progress",
            Long.MAX_VALUE, capturedSession = Session(1L, "test@example.com", "test-token", Long.MAX_VALUE)))
    }
    @Test fun refusesToSendCapturedRequestWithAnotherAccountsToken() {
        assertThrows(IOException::class.java) {
            headerFor("gamification/me/progress", Long.MAX_VALUE,
                capturedSession = Session(2L, "other@example.invalid", "other-token", Long.MAX_VALUE))
        }
    }
}
