package pe.greenminds.ecomind.quests

import androidx.compose.runtime.*
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import pe.greenminds.ecomind.quests.application.service.QuestExecution
import pe.greenminds.ecomind.quests.domain.entity.*
import pe.greenminds.ecomind.quests.domain.valueobject.*
import pe.greenminds.ecomind.quests.presentation.ui.execution.QuestExecutionContent
import pe.greenminds.ecomind.quests.presentation.viewmodel.*
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme

class QuestExecutionScreenTest {
    @get:Rule val compose = createComposeRule()
    @Test fun individualQuestRequiresEveryCheckboxBeforeFinish() {
        val quest = Quest(1, 1, 1, QuestPublicationStatus.PUBLISHED, null, "Time to save up energy!",
            "Turn off your lights and unplug unused devices.", QuestCategory.ENERGY, QuestType.ACTIVITIES,
            QuestReward(0, 50), null, 60, QuestTheme.CHECKBOX, null, null)
        val activities = listOf(Activity(1, 1, "Turn off your lights", 1, ActivityType.CHECKBOX, emptyMap(), null),
            Activity(2, 1, "Unplug your devices", 2, ActivityType.CHECKBOX, emptyMap(), null))
        var state by mutableStateOf(QuestExecutionState(QuestExecution(quest, activities, null, emptyList())))
        var openedRewards = false
        compose.setContent {
            EcoMindTheme {
                QuestExecutionContent(state, {}, {}, {
                    state = state.copy(stage = QuestStage.ACTIVITIES, execution = state.execution!!.copy(
                        assignment = QuestUser(1, 1, 1, QuestStatus.IN_PROGRESS, 0.0, null),
                        checks = activities.map { ActivityUser(it.id, 1, it.id, 0.0, null, it.description, emptyMap()) }))
                }, { id, checked ->
                    val execution = state.execution!!
                    val checks = execution.checks.map { if (it.id == id) it.copy(progress = if (checked) 100.0 else 0.0) else it }
                    val progress = checks.map { it.progress }.average()
                    state = state.copy(execution = execution.copy(checks = checks, assignment = execution.assignment!!.copy(
                        progress = progress, status = if (progress == 100.0) QuestStatus.READY_TO_COMPLETE else QuestStatus.IN_PROGRESS)))
                }, { state = state.copy(stage = QuestStage.FINISHED) }, { openedRewards = true })
            }
        }
        compose.onNodeWithText("Invite friends").assertDoesNotExist()
        screenshot("quest-detail.png")
        compose.onNodeWithText("Start Quest").performClick()
        compose.onNodeWithText("Finish Quest").assertIsNotEnabled()
        compose.onAllNodes(isToggleable())[0].performClick()
        compose.onNodeWithText("50% completed").assertExists()
        compose.onNodeWithText("Finish Quest").assertIsNotEnabled()
        compose.onAllNodes(isToggleable())[1].performClick()
        screenshot("quest-activities.png")
        compose.onNodeWithText("Finish Quest").assertIsEnabled().performClick()
        compose.onNodeWithText("Quest completed!\nGood job!").assertExists()
        compose.onNodeWithText("+50 ecoPoints").assertExists()
        compose.onNodeWithText("View rewards and achievements").performClick()
        compose.runOnIdle { org.junit.Assert.assertTrue(openedRewards) }
        screenshot("quest-finished.png")
    }
    private fun screenshot(name: String) {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        File(context.getExternalFilesDir(null), name).outputStream().use {
            compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
