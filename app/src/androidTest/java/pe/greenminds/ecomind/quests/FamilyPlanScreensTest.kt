package pe.greenminds.ecomind.quests

import android.graphics.Bitmap
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.main.MainNavigationBar
import pe.greenminds.ecomind.main.NavigationItem
import pe.greenminds.ecomind.quests.application.*
import pe.greenminds.ecomind.quests.domain.entity.*
import pe.greenminds.ecomind.quests.domain.valueobject.*
import pe.greenminds.ecomind.quests.presentation.states.*
import pe.greenminds.ecomind.quests.presentation.ui.family.*
import pe.greenminds.ecomind.quests.presentation.ui.execution.QuestExecutionContent
import pe.greenminds.ecomind.quests.presentation.viewmodel.QuestExecutionState
import pe.greenminds.ecomind.quests.presentation.viewmodel.QuestStage
import pe.greenminds.ecomind.quests.application.service.QuestExecution
import pe.greenminds.ecomind.quests.presentation.ui.progress.ProgressContent
import pe.greenminds.ecomind.shared.interfaces.components.EcoMindTopBar
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.users.domain.model.*
import java.io.File

class FamilyPlanScreensTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private fun text(id: Int) = context.getString(id)
    private val member = FamilyMembership(8, 2, 20, FamilyRole.PARENT)
    private val quest = Quest(3, 3, 1, QuestPublicationStatus.PUBLISHED, null, "Time to save up energy!",
        "We will turn off the lights and unplug devices we are not using.", QuestCategory.ENERGY, QuestType.FAMILY,
        QuestReward(3, 50), null, 60, QuestTheme.COLLABORATIVE, null, null)
    private val water = quest.copy(id = 4, title = "Save water together", category = QuestCategory.WATER, theme = QuestTheme.CHECKBOX)
    private val plan = FamilyPlan(9, 2, 20, FamilyPlanStatus.DRAFT, 0.0, emptyList())
    private fun state(plan: FamilyPlan = this.plan) = FamilyPlansUiState(FamilyPlanOverview(member, listOf(plan)),
        FamilyPlanDetails(plan, member, mapOf(3L to quest, 4L to water)), listOf(quest, water))

    @Composable
    private fun Frame(content: @Composable (Modifier) -> Unit) {
        EcoMindTheme {
            Scaffold(topBar = { EcoMindTopBar(360, 16, {}, {}) },
                bottomBar = { MainNavigationBar(NavigationItem.PROGRESS, {}) }) { padding ->
                content(Modifier.padding(padding))
            }
        }
    }

    @Test fun builderAddsManagesAndStartsOnlyANonemptyPlan() {
        var ui by mutableStateOf(state())
        var started = false
        var saved = false
        var loaded = false
        compose.setContent {
            Frame { modifier -> androidx.compose.foundation.layout.Box(modifier) {
                FamilyPlanBuilderContent(ui, {}, { loaded = true }, { saved = true }, { started = true }, {}, { _, _ -> },
                    onAdd = { id ->
                        val updated = ui.details!!.plan.copy(items = ui.details!!.plan.items + FamilyPlanItem(id, id, null, 0.0))
                        ui = ui.copy(details = ui.details!!.copy(plan = updated))
                    }, onRemove = { id -> ui = ui.copy(details = ui.details!!.copy(plan = ui.details!!.plan.copy(
                        items = ui.details!!.plan.items.filterNot { it.questId == id }))) }, onMove = { _, _ -> })
            } }
        }
        compose.onNodeWithText(text(R.string.family_plan_start)).assertIsNotEnabled()
        screenshot("family-builder.png")
        compose.onAllNodesWithText(text(R.string.family_plan_add))[0].performClick()
        compose.onNodeWithText(text(R.string.family_plan_start)).assertIsEnabled()
        compose.onNodeWithText(text(R.string.family_plan_manage_tab)).performClick()
        compose.onNodeWithContentDescription(text(R.string.family_plan_remove)).assertIsDisplayed()
        screenshot("family-manage.png")
        compose.onNodeWithText(text(R.string.family_plan_save)).performClick()
        compose.onNodeWithText(text(R.string.family_plan_load)).performClick()
        compose.onNodeWithText(text(R.string.family_plan_start)).performClick()
        compose.runOnIdle { assertTrue(saved); assertTrue(loaded); assertTrue(started) }
    }

    @Test fun deletingASavedPlanRequiresConfirmation() {
        val ui = state(plan.copy(items = listOf(FamilyPlanItem(11, 3, null, 0.0))))
        var deleting by mutableStateOf(false)
        var deleted = false
        var loaded: Long? = null
        compose.setContent {
            Frame { modifier -> androidx.compose.foundation.layout.Box(modifier) {
                FamilyPlanListContent(ui, {}, { loaded = it.id }, { deleting = true }, {})
                if (deleting) FamilyPlanDeleteDialog(false, false, false, { deleting = false }, { deleted = true; deleting = false })
            } }
        }
        compose.onNodeWithText(text(R.string.family_plan_load)).performClick()
        compose.runOnIdle { assertEquals(9L, loaded) }
        screenshot("family-load.png")
        compose.onNodeWithContentDescription(text(R.string.family_plan_delete)).performClick()
        screenshot("family-delete.png")
        compose.onNodeWithText(text(R.string.family_plan_no)).performClick()
        compose.runOnIdle { assertFalse(deleted) }
        compose.onNodeWithContentDescription(text(R.string.family_plan_delete)).performClick()
        compose.onNodeWithText(text(R.string.family_plan_yes)).performClick()
        compose.runOnIdle { assertTrue(deleted) }
    }

    @Test fun progressOffersAPlanWhenParentHasNoActivePlan() {
        var created = false
        compose.setContent {
            Frame { modifier -> ProgressContent(ProgressUiState(isLoading = false), modifier,
                familyState = FamilyPlansUiState(overview = FamilyPlanOverview(member, emptyList())),
                onCreateFamilyPlan = { created = true }) }
        }
        compose.onNodeWithText(text(R.string.family_plan_new)).performScrollTo().assertIsDisplayed()
        screenshot("family-progress-empty.png")
        compose.onNodeWithText(text(R.string.family_plan_new)).performClick()
        compose.runOnIdle { assertTrue(created) }
    }

    @Test fun completedFamilyExecutionIsReadOnlyAndReturnsToThePlan() {
        val active = plan.copy(status = FamilyPlanStatus.COMPLETED, progress = 100.0,
            items = listOf(FamilyPlanItem(11, 3, 45, 100.0)))
        val details = state(active).details!!
        val activity = Activity(4, 3, "Unplug unused devices", 1, ActivityType.CHECKBOX, emptyMap(), null)
        val execution = FamilyQuestExecution(details, active.items.single(), quest,
            QuestUser(70, 20, 3, QuestStatus.COMPLETED, 100.0, null, 45), listOf(activity),
            listOf(ActivityUser(80, 70, 4, 100.0, null, activity.description, emptyMap(), 45)))
        var returned = false
        compose.setContent {
            Frame { modifier -> androidx.compose.foundation.layout.Box(modifier) {
                QuestExecutionContent(QuestExecutionState(
                    execution = QuestExecution(execution.quest, execution.activities, execution.assignment, execution.checks, active.id),
                    stage = QuestStage.ACTIVITIES, familyExecution = execution
                ), { returned = true }, {}, {}, { _, _ -> fail("Completed plan must be read only") }, { returned = true })
            } }
        }
        compose.onNode(isToggleable()).assertIsNotEnabled()
        compose.onNodeWithText("Finish Quest").performClick()
        compose.runOnIdle { assertTrue(returned) }
        screenshot("family-activities.png")
    }

    private fun screenshot(name: String) {
        compose.waitForIdle()
        val bitmap = requireNotNull(InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot())
        File(context.getExternalFilesDir(null), name).outputStream().use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }
}
