package pe.greenminds.ecomind.iam.terms

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.util.Locale
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.iam.interfaces.terms.TermsScreen
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme

class TermsScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun englishTermsCanBeReadToTheEndAndClosed() {
        checkTerms(Locale.US, 1f, "en")
    }

    @Test
    fun spanishTermsRemainScrollableWithLargeText() {
        checkTerms(Locale.forLanguageTag("es-419"), 1.5f, "es-large")
    }

    private fun checkTerms(locale: Locale, fontScale: Float, name: String) {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val base = instrumentation.targetContext
        val configuration = Configuration(base.resources.configuration).apply { setLocale(locale) }
        val context = base.createConfigurationContext(configuration)
        var returned = false
        compose.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(
                LocalContext provides context,
                LocalConfiguration provides configuration,
                LocalDensity provides Density(density, fontScale)
            ) {
                EcoMindTheme { TermsScreen(onBack = { returned = true }) }
            }
        }
        compose.onNodeWithText(context.getString(R.string.terms_title)).assertIsDisplayed()
        capture("$name-top")
        compose.onNodeWithText(context.getString(R.string.back)).performScrollTo().assertIsDisplayed()
        capture("$name-bottom")
        compose.onNodeWithText(context.getString(R.string.back)).performClick()
        compose.runOnIdle { assertTrue(returned) }
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val screenshot = instrumentation.uiAutomation.takeScreenshot()
        val directory = File(instrumentation.targetContext.getExternalFilesDir(null), "terms-evidence")
        directory.mkdirs()
        File(directory, "$name.png").outputStream().use {
            screenshot.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        screenshot.recycle()
    }
}
