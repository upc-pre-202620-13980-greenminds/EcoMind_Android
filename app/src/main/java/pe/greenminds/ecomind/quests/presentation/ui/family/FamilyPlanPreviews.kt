package pe.greenminds.ecomind.quests.presentation.ui.family

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import pe.greenminds.ecomind.quests.application.FamilyPlanDetails
import pe.greenminds.ecomind.quests.application.FamilyPlanOverview
import pe.greenminds.ecomind.quests.domain.entity.*
import pe.greenminds.ecomind.quests.domain.valueobject.*
import pe.greenminds.ecomind.quests.presentation.states.FamilyPlansUiState
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.users.domain.model.FamilyMembership
import pe.greenminds.ecomind.users.domain.model.FamilyRole

private fun familyPreviewState(): FamilyPlansUiState {
    val member = FamilyMembership(1, 1, 1, FamilyRole.PARENT)
    val quests = listOf(
        Quest(1, 1, 1, QuestPublicationStatus.PUBLISHED, null, "Time to save up energy!",
            "We will turn off the lights and unplug devices we are not using.", QuestCategory.ENERGY,
            QuestType.FAMILY, QuestReward(3, 50), null, 60, QuestTheme.COLLABORATIVE, null, null),
        Quest(2, 2, 1, QuestPublicationStatus.PUBLISHED, null, "Save water together",
            "Check the taps at home and find ways to save water as a family.", QuestCategory.WATER,
            QuestType.FAMILY, QuestReward(3, 50), null, 20, QuestTheme.CHECKBOX, null, null)
    )
    val plan = FamilyPlan(1, 1, 1, FamilyPlanStatus.DRAFT, 0.0, listOf(FamilyPlanItem(1, 1, null, 0.0)))
    return FamilyPlansUiState(FamilyPlanOverview(member, listOf(plan, plan.copy(id = 2), plan.copy(id = 3))),
        FamilyPlanDetails(plan, member, quests.associateBy { it.id }), catalog = quests)
}

@Preview(name = "Add family quests", locale = "en", showBackground = true, widthDp = 360, heightDp = 640)
@Preview(name = "Agregar retos familiares", locale = "es-rPE", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun FamilyBuilderPreview() {
    EcoMindTheme {
        FamilyPlanBuilderContent(familyPreviewState(), {}, {}, {}, {}, {}, { _, _ -> }, {}, {}, { _, _ -> })
    }
}

@Preview(name = "Saved family plans", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun FamilyListPreview() {
    EcoMindTheme { FamilyPlanListContent(familyPreviewState(), {}, {}, {}, {}) }
}

@Preview(name = "Family plan progress", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun FamilyDetailPreview() {
    val state = familyPreviewState()
    val details = requireNotNull(state.details)
    EcoMindTheme {
        FamilyPlanDetailContent(state.copy(details = details.copy(plan = details.plan.copy(
            status = FamilyPlanStatus.ACTIVE, progress = 50.0,
            items = details.plan.items.map { it.copy(progress = 50.0, collaborativeSessionId = 1) }
        ))), {}, { _, _ -> }, {}, {}, {}, {})
    }
}
