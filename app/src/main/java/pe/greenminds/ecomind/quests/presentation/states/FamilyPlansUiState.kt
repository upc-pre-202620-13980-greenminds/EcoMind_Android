package pe.greenminds.ecomind.quests.presentation.states

import pe.greenminds.ecomind.quests.application.FamilyPlanDetails
import pe.greenminds.ecomind.quests.application.FamilyPlanOverview
import pe.greenminds.ecomind.quests.domain.entity.Quest
import pe.greenminds.ecomind.quests.domain.valueobject.QuestCategory
import pe.greenminds.ecomind.quests.domain.valueobject.QuestTheme

data class FamilyPlansUiState(
    val overview: FamilyPlanOverview? = null,
    val details: FamilyPlanDetails? = null,
    val catalog: List<Quest> = emptyList(),
    val query: String = "",
    val category: QuestCategory? = null,
    val theme: QuestTheme? = null,
    val busy: Boolean = false,
    val searching: Boolean = false,
    val error: FamilyPlanUiError? = null,
    val searchError: FamilyPlanUiError? = null
) {
    val selectedQuestIds: Set<Long> get() = details?.plan?.items.orEmpty().map { it.questId }.toSet()
}
