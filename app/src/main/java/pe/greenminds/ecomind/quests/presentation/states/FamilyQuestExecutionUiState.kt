package pe.greenminds.ecomind.quests.presentation.states

import pe.greenminds.ecomind.quests.application.FamilyQuestExecution

data class FamilyQuestExecutionUiState(
    val execution: FamilyQuestExecution? = null,
    val busy: Boolean = false,
    val error: FamilyPlanUiError? = null
)
