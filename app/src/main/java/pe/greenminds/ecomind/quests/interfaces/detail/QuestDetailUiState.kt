package pe.greenminds.ecomind.quests.interfaces.detail
import pe.greenminds.ecomind.quests.domain.model.QuestExecution
data class QuestDetailUiState(val loading: Boolean = true, val busy: Boolean = false, val execution: QuestExecution? = null,
    val error: Boolean = false, val sessionRequired: Boolean = false)
