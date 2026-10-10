package pe.greenminds.ecomind.gamification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Test
import pe.greenminds.ecomind.gamification.infrastructure.di.GamificationApiModule
import pe.greenminds.ecomind.iam.domain.model.Session
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.iam.infrastructure.remote.AuthInterceptor
import pe.greenminds.ecomind.monetization.infrastructure.di.MonetizationApiModule
import pe.greenminds.ecomind.quests.infrastructure.di.QuestsApiModule
import pe.greenminds.ecomind.shared.infrastructure.di.NetworkModule
import pe.greenminds.ecomind.users.infrastructure.di.UsersApiModule

class SharedConnectionTests {
    @Test fun featureApiModulesUseOneConfiguredRetrofitAndOneIamAuthorizationHeader() = runBlocking {
        val server = MockWebServer().apply { start() }
        try {
            val session = Session(1, "student@example.invalid", "same-session-token", Long.MAX_VALUE)
            val sessions = object : SessionRepository {
                override fun getSession() = MutableStateFlow<Session?>(session)
                override suspend fun saveSession(session: Session) = Unit
                override suspend fun clearSession() = Unit
            }
            val client = NetworkModule.provideOkHttpClient(AuthInterceptor(sessions)).newBuilder()
                .addInterceptor { chain ->
                    val request = chain.request()
                    chain.proceed(request.newBuilder().url(server.url(request.url.encodedPath)).build())
                }.build()
            val retrofit = NetworkModule.provideRetrofit(client)
            fun respond(body: String) {
                server.enqueue(MockResponse().setBody(body).addHeader("Content-Type", "application/json"))
            }

            respond("""{"userId":1,"totalEcopoints":25,"currentStreak":2,"longestStreak":3}""")
            assertEquals(25L, GamificationApiModule.provideGamificationApi(retrofit).progress(session).totalEcopoints)
            respond("[]")
            assertEquals(emptyList<Any>(), QuestsApiModule.provideQuestApi(retrofit).getQuests().body())
            respond("""{"balance":7}""")
            assertEquals(7, MonetizationApiModule.provideMonetizationApi(retrofit).getWallet().body()?.balance)
            respond("""{"id":1,"name":"Student","socialRole":"STUDENT","streak":2,"ecopoints":25,"gemBalance":7}""")
            assertEquals(1L, UsersApiModule.provideUsersApi(retrofit).profile(session, 1).id)

            val paths = listOf("gamification/me/progress", "quests", "monetization/me/wallet", "user/1")
            paths.forEach { path ->
                val request = server.takeRequest()
                assertEquals("/api/v1/$path", request.path)
                assertEquals(listOf("Bearer same-session-token"), request.headers.values("Authorization"))
                assertEquals("", request.body.readUtf8())
            }
        } finally {
            server.shutdown()
        }
    }
}
