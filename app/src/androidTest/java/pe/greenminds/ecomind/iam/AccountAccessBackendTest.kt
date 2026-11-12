package pe.greenminds.ecomind.iam

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONObject
import org.junit.Assert.assertNotNull
import pe.greenminds.ecomind.iam.domain.model.EmailAddress
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import pe.greenminds.ecomind.MainActivity
import pe.greenminds.ecomind.R
import java.io.File

class AccountAccessBackendTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun recoveryRequestShowsTheGenericAcknowledgement() {
        val arguments = InstrumentationRegistry.getArguments()
        assumeTrue(arguments.getString("iamRecoveryRequestTest") == "true")
        val email = readPrivateFixture().getString("email")

        try {
            awaitSignIn(timeoutMillis = 60_000)
        } catch (failure: Throwable) {
            capture("recovery-request-sign-in-timeout")
            throw failure
        }

        compose.onNodeWithText(text(R.string.sign_in_forgot_password)).performClick()
        compose.onNodeWithContentDescription(text(R.string.sign_in_email_label)).performTextInput(email)
        compose.onNodeWithText(text(R.string.recovery_request_button)).performScrollTo().performClick()
        compose.waitUntil(120_000) {
            compose.onAllNodesWithText(text(R.string.recovery_check_email_title)).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText(text(R.string.recovery_check_email_title)).assertIsDisplayed()
        compose.onNodeWithText(
            compose.activity.getString(
                R.string.recovery_check_email_description,
                EmailAddress.mask(EmailAddress.normalize(email))
            )
        ).assertIsDisplayed()
    }

    @Test
    fun resetPasswordThenSignInAndLogOutThroughTheApi() {
        val arguments = InstrumentationRegistry.getArguments()
        assumeTrue(arguments.getString("iamBackendTest") == "true")
        // Keep live credentials out of instrumentation arguments and Gradle logs. The
        // host pushes this JSON into the app's private external files directory and
        // passes only its path as `iamFixturePath`.
        val fixture = if (arguments.getString("iamFixturePath") != null) readPrivateFixture() else null
        val link = fixture?.getString("recoveryLink") ?: requireNotNull(arguments.getString("iamRecoveryLink"))
        val email = fixture?.getString("email") ?: requireNotNull(arguments.getString("iamEmail"))
        val password = fixture?.getString("newPassword") ?: requireNotNull(arguments.getString("iamNewPassword"))
        compose.activityRule.scenario.onActivity { activity ->
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link)).setPackage(activity.packageName)
            assertNotNull("Recovery URL must resolve through the app manifest", activity.packageManager.resolveActivity(intent, 0))
            activity.startActivity(intent)
        }
        compose.waitUntil(20_000) {
            compose.onAllNodesWithText(text(R.string.recovery_reset_title)).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithContentDescription(text(R.string.recovery_new_password_label))
            .performTextInput(password)
        compose.onNodeWithContentDescription(text(R.string.sign_up_confirm_password_label))
            .performTextInput(password)
        compose.onNodeWithText(text(R.string.recovery_reset_button)).performScrollTo().performClick()
        compose.waitUntil(120_000) {
            compose.onAllNodesWithText(text(R.string.recovery_completed_title)).fetchSemanticsNodes().isNotEmpty()
        }
        capture("password-updated")
        compose.onNodeWithText(text(R.string.sign_in_button)).performScrollTo().performClick()
        awaitSignIn()
        compose.onNodeWithContentDescription(text(R.string.sign_in_email_label)).performTextInput(email)
        compose.onNodeWithContentDescription(text(R.string.sign_in_password_label)).performTextInput(password)
        compose.onNodeWithText(text(R.string.sign_in_button)).performScrollTo().performClick()
        compose.waitUntil(120_000) {
            compose.onAllNodesWithContentDescription(text(R.string.top_bar_settings)).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithContentDescription(text(R.string.top_bar_settings)).performClick()
        compose.onNodeWithText(text(R.string.terms_title)).performScrollTo().performClick()
        compose.waitUntil(20_000) {
            compose.onAllNodesWithText(text(R.string.terms_body)).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithContentDescription(text(R.string.back)).performClick()
        compose.waitUntil(20_000) {
            compose.onAllNodesWithText(text(R.string.settings_language)).fetchSemanticsNodes().isNotEmpty()
        }
        capture("settings")
        compose.onNodeWithText(text(R.string.log_out)).performScrollTo().performClick()
        compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        compose.waitUntil(120_000) {
            compose.onAllNodesWithText(text(R.string.sign_in_forgot_password)).fetchSemanticsNodes().isNotEmpty()
        }
        compose.activityRule.scenario.recreate()
        awaitSignIn()
        compose.onNodeWithText(text(R.string.sign_in_forgot_password)).assertIsDisplayed()
        capture("signed-out")
    }

    private fun text(resource: Int) = compose.activity.getString(resource)

    private fun readPrivateFixture(): JSONObject {
        val fixturePath = requireNotNull(InstrumentationRegistry.getArguments().getString("iamFixturePath"))
        val privateDirectory = requireNotNull(
            InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null)
        ).canonicalFile
        val fixtureFile = File(fixturePath).canonicalFile
        require(fixtureFile.parentFile == privateDirectory) {
            "IAM fixture must be stored in the app's private external files directory"
        }
        return JSONObject(fixtureFile.readText())
    }

    private fun awaitSignIn(timeoutMillis: Long = 30_000) {
        compose.waitUntil(timeoutMillis) {
            runCatching {
                compose.onNodeWithText(text(R.string.sign_in_forgot_password)).assertIsDisplayed()
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
