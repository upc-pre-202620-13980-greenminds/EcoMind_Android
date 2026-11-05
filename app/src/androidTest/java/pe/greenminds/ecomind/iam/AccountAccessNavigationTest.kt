package pe.greenminds.ecomind.iam

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import pe.greenminds.ecomind.MainActivity
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.iam.interfaces.navigation.PasswordResetLink
import java.io.File

class AccountAccessNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun termsReturnToTheRegistrationWithoutLosingTheForm() {
        awaitText(R.string.sign_in_sign_up_link)
        compose.onNodeWithText(text(R.string.sign_in_sign_up_link)).performClick()
        compose.onNodeWithContentDescription(text(R.string.sign_up_name_label))
            .performTextInput("Test Student")
        compose.onNodeWithText(text(R.string.sign_up_terms_link)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.terms_title)).assertIsDisplayed()
        capture("terms")
        compose.onNodeWithContentDescription(text(R.string.back)).performClick()
        compose.onNodeWithText("Test Student").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun recoveryCanReturnToSignIn() {
        awaitText(R.string.sign_in_forgot_password)
        compose.onNodeWithText(text(R.string.sign_in_forgot_password)).performClick()
        compose.onNodeWithText(text(R.string.recovery_request_title)).assertIsDisplayed()
        capture("password-recovery")
        compose.onNodeWithContentDescription(text(R.string.back)).performClick()
        awaitText(R.string.sign_in_forgot_password)
        compose.onNodeWithText(text(R.string.sign_in_forgot_password)).assertIsDisplayed()
    }

    @Test
    fun aRecoveryLinkWithoutATokenShowsTheInvalidLinkState() {
        compose.activityRule.scenario.onActivity { activity ->
            activity.startActivity(Intent(
                Intent.ACTION_VIEW,
                Uri.parse(PasswordResetLink.BASE_URI),
                activity,
                MainActivity::class.java
            ))
        }
        awaitText(R.string.recovery_invalid_link_title)
        compose.onNodeWithText(text(R.string.recovery_invalid_link_title)).assertIsDisplayed()
    }

    @Test
    fun anIncomingLinkOpensResetWhileTheApplicationIsRunning() {
        compose.activityRule.scenario.onActivity { activity ->
            activity.startActivity(Intent(
                Intent.ACTION_VIEW,
                Uri.parse("${PasswordResetLink.BASE_URI}?token=navigation-test-token"),
                activity,
                MainActivity::class.java
            ))
        }
        awaitText(R.string.recovery_reset_title)
        compose.onNodeWithText(text(R.string.recovery_reset_title)).assertIsDisplayed()
        compose.onNodeWithContentDescription(text(R.string.recovery_new_password_label))
            .performTextInput("GreenPlanet2026")
        capture("password-reset")
        compose.activityRule.scenario.recreate()
        awaitText(R.string.recovery_reset_title)
        compose.onNodeWithText(text(R.string.recovery_reset_title)).assertIsDisplayed()
        compose.onNodeWithText(text(R.string.recovery_request_new_button)).performScrollTo().performClick()
        compose.onNodeWithText(text(R.string.recovery_request_title)).assertIsDisplayed()
        compose.onNodeWithContentDescription(text(R.string.back)).performClick()
        awaitText(R.string.sign_in_forgot_password)
        compose.onNodeWithText(text(R.string.sign_in_forgot_password)).assertIsDisplayed()
    }

    private fun text(resource: Int) = compose.activity.getString(resource)

    private fun awaitText(resource: Int) {
        compose.waitUntil(20_000) {
            runCatching {
                compose.onNodeWithText(text(resource)).assertIsDisplayed()
                true
            }.getOrDefault(false)
        }
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val screenshot = instrumentation.uiAutomation.takeScreenshot()
        val directory = File(instrumentation.targetContext.getExternalFilesDir(null), "iam-evidence")
        directory.mkdirs()
        File(directory, "$name.png").outputStream().use {
            screenshot.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        screenshot.recycle()
    }
}
