package pe.greenminds.ecomind.quests.presentation.states

import pe.greenminds.ecomind.quests.application.ProgressEntry

data class ProgressUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val hasLoaded: Boolean = false,
    val hasError: Boolean = false,
    val inProgress: List<ProgressEntry> = emptyList(),
    val familyQuests: List<ProgressEntry> = emptyList(),
    val finished: List<ProgressEntry> = emptyList(),
    val history: List<ProgressEntry> = finished,
    val isUpdating: Boolean = false
)
