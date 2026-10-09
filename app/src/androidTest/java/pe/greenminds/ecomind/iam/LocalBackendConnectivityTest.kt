package pe.greenminds.ecomind.iam

import android.Manifest
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assume.assumeTrue
import org.junit.Test
import pe.greenminds.ecomind.iam.infrastructure.remote.AuthApi
import pe.greenminds.ecomind.iam.infrastructure.remote.SignInRequest
import pe.greenminds.ecomind.shared.infrastructure.di.NetworkModule

class LocalBackendConnectivityTest {
    // Opt-in: needs a running local backend and never uses a real account or JWT.
    @Test
    fun retrofitCanReachLocalSignIn() = runBlocking {
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
        val retrofit = NetworkModule.provideRetrofit(NetworkModule.provideOkHttpClient())
        val api = retrofit.create(AuthApi::class.java)
        val response = api.signIn(SignInRequest("network-probe@example.invalid", "invalid"))
        assertEquals(401, response.code())
    }
}
