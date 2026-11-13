package pe.greenminds.ecomind.gamification.interfaces.progress

import pe.greenminds.ecomind.gamification.domain.model.RewardHistory

data class HistoryUiState(
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val sessionRequired: Boolean = false,
    val rewards: List<RewardHistory> = emptyList()
)
