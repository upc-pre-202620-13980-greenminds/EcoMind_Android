package pe.greenminds.ecomind.gamification

import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.Espresso
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import pe.greenminds.ecomind.BuildConfig
import pe.greenminds.ecomind.MainActivity
import pe.greenminds.ecomind.R
import java.io.File

/** Run explicitly against the isolated GamificationDemoServer, never a shared deployment. */
class RemoteGamificationFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private fun label(id: Int) = InstrumentationRegistry.getInstrumentation().targetContext.getString(id)
    private fun waitText(text: String) { compose.waitUntil(30000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() } }
    private fun pace() { if (InstrumentationRegistry.getArguments().getString("demo") == "true") Thread.sleep(1500) }
    private fun click(text: String) { pace(); compose.waitUntil(30000) { compose.onAllNodes(hasText(text) and hasClickAction()).fetchSemanticsNodes().isNotEmpty() }; compose.onAllNodes(hasText(text) and hasClickAction()).onFirst().performClick() }
    private fun back() { compose.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() } }
    private fun screenshot(name: String) {
        compose.onRoot().captureToImage().asAndroidBitmap().let { bitmap ->
            val directory = InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null)!!
            File(directory, name).outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
    }

    @Test fun activityCompletionUpdatesProgressAndAchievementAndPublishesOnce() {
        assumeTrue(BuildConfig.REMOTE_BACKEND && BuildConfig.API_BASE_URL.contains(":8095/"))
        if (InstrumentationRegistry.getArguments().getString("reuseSession") != "true") {
        waitText(label(R.string.sign_in_button))
        compose.onAllNodes(hasSetTextAction())[0].performTextInput("qa.parent@example.test")
        compose.onAllNodes(hasSetTextAction())[1].performTextInput("QaGreen2026")
        Espresso.closeSoftKeyboard()
        click(label(R.string.sign_in_button))
        }
        click(label(R.string.main_menu_energy))
        click(label(R.string.quest_daily_quest))
        click("Check unused lights")
        waitText(label(R.string.quest_execution_start))
        click(label(R.string.quest_execution_start))
        waitText("I checked the lights in an empty room")
        compose.onNode(isToggleable()).performClick()
        compose.waitUntil(30000) { compose.onNodeWithText(label(R.string.quest_execution_finish)).fetchSemanticsNode().config.contains(androidx.compose.ui.semantics.SemanticsProperties.Disabled).not() }
        click(label(R.string.quest_execution_finish))
        waitText(label(R.string.quest_execution_completed))
        screenshot("remote-activity-completed.png")
        click(label(R.string.gamification_progress))
        waitText(label(R.string.gamification_streak))
        screenshot("remote-progress.png")
        click(label(R.string.gamification_history))
        waitText(label(R.string.gamification_source_quest))
        compose.onNodeWithText("10 ecopoints · 2 gems").assertIsDisplayed()
        screenshot("remote-reward-history.png")
        back()
        click(label(R.string.achievements_title))
        click("First activity")
        click(label(R.string.gamification_share))
        click("QA Garden")
        click(label(R.string.gamification_share))
        // The dialog and screen use the same label; choose the dialog's button.
        compose.onAllNodes(hasText(label(R.string.gamification_share)) and hasClickAction()).onLast().performClick()
        compose.waitUntil(30000) {
            compose.onAllNodesWithText(label(R.string.gamification_pending)).fetchSemanticsNodes().isNotEmpty() ||
                compose.onAllNodesWithText(label(R.string.gamification_published)).fetchSemanticsNodes().isNotEmpty()
        }
        screenshot("remote-share-status.png")
        repeat(8) {
            if (compose.onAllNodesWithText(label(R.string.gamification_published)).fetchSemanticsNodes().isEmpty()) {
                click(label(R.string.gamification_refresh))
                compose.waitForIdle()
                Thread.sleep(1000)
            }
        }
        waitText(label(R.string.gamification_published))
        screenshot("remote-share-published.png")
        pace(); pace()
    }
}
