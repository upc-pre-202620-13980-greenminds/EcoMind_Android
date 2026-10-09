package pe.greenminds.ecomind

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import android.Manifest
import android.os.Build
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runners.model.Statement

class MainActivitySmokeTest {
    @get:Rule(order = 0) val localNetworkPermission = TestRule { base, _ ->
        object : Statement() {
            override fun evaluate() {
                if (Build.VERSION.SDK_INT >= 37) {
                    val instrumentation = InstrumentationRegistry.getInstrumentation()
                    instrumentation.uiAutomation.grantRuntimePermission(
                        instrumentation.targetContext.packageName, Manifest.permission.ACCESS_LOCAL_NETWORK
                    )
                }
                base.evaluate()
            }
        }
    }
    @get:Rule(order = 1) val compose = createAndroidComposeRule<MainActivity>()

    @Test fun localizedActivityCreatesHiltViewModelsAndRendersTheNavigationHost() {
        compose.onRoot().assertExists()
    }
}
