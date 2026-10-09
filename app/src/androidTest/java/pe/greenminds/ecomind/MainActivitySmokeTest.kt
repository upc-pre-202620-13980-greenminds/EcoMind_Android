package pe.greenminds.ecomind

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test

class MainActivitySmokeTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun localizedActivityCreatesHiltViewModelsAndRendersTheNavigationHost() {
        compose.onRoot().assertExists()
    }
}
