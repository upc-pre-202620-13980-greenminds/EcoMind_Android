package pe.greenminds.ecomind.iam

import android.Manifest
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test
import pe.greenminds.ecomind.iam.infrastructure.remote.AuthApi
import pe.greenminds.ecomind.iam.infrastructure.remote.AuthInterceptor
import pe.greenminds.ecomind.iam.infrastructure.local.SessionDataStore
import pe.greenminds.ecomind.iam.infrastructure.di.IamDataStoreModule
import pe.greenminds.ecomind.iam.infrastructure.mapper.toDomain
import pe.greenminds.ecomind.quests.infrastructure.remote.QuestApi
import pe.greenminds.ecomind.quests.infrastructure.implementation.RemoteQuestRepository
import pe.greenminds.ecomind.iam.infrastructure.remote.SignInRequest
import pe.greenminds.ecomind.shared.infrastructure.di.NetworkModule

class LocalBackendConnectivityTest {
    // Opt-in: requires a local backend. Credentials, when needed, come from runner arguments.
    @Test
    fun retrofitCanReachLocalSignIn(): Unit = runBlocking {
        assumeTrue(
            InstrumentationRegistry.getArguments().getString("localBackendSmokeTest") == "true"
        )
        if (Build.VERSION.SDK_INT >= 37) {
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            instrumentation.uiAutomation.grantRuntimePermission(
                instrumentation.targetContext.packageName,
                Manifest.permission.ACCESS_LOCAL_NETWORK
            )
        }
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val sessions = SessionDataStore(IamDataStoreModule.provideSessionDataStore(context))
        val retrofit = NetworkModule.provideRetrofit(
            NetworkModule.provideOkHttpClient(AuthInterceptor(sessions))
        )
        val api = retrofit.create(AuthApi::class.java)
        val response = api.signIn(SignInRequest("network-probe@example.invalid", "invalid"))
        assertEquals(401, response.code())
        val arguments = InstrumentationRegistry.getArguments()
        val email = arguments.getString("localBackendEmail")
        val password = arguments.getString("localBackendPassword")
        if (email != null && password != null) {
            val login = api.signIn(SignInRequest(email, password))
            assertEquals(200, login.code())
            sessions.saveSession(requireNotNull(login.body()).toDomain())
        }
        val quests = RemoteQuestRepository(retrofit.create(QuestApi::class.java))
        val catalog = quests.getQuests().getOrThrow()
        // Empty catalogs are valid; when data exists, verify search and detail against it.
        catalog.firstOrNull()?.let { quest ->
            assertEquals(quest, quests.getQuest(quest.id).getOrThrow())
            val matches = quests.searchQuests(quest.title, quest.category, quest.type).getOrThrow()
            org.junit.Assert.assertTrue(matches.any { it.id == quest.id })
        }
    }
}
