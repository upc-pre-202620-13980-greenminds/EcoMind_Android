package pe.greenminds.ecomind.gamification.interfaces.progress

import pe.greenminds.ecomind.gamification.domain.model.GamificationOverview

data class ProgressUiState(
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val sessionRequired: Boolean = false,
    val overview: GamificationOverview? = null
)
