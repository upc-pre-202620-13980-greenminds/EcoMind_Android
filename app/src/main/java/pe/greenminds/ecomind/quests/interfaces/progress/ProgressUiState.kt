package pe.greenminds.ecomind.quests.interfaces.progress

import pe.greenminds.ecomind.quests.domain.model.ProgressEntry

data class ProgressUiState(
    val isLoading: Boolean = true,
    val inProgress: List<ProgressEntry> = emptyList(),
    val familyQuests: List<ProgressEntry> = emptyList(),
    val finished: List<ProgressEntry> = emptyList()
)
