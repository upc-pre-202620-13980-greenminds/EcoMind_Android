package pe.greenminds.ecomind.gamification.interfaces.achievements

import pe.greenminds.ecomind.gamification.domain.model.AchievementCollection

data class AchievementsUiState(
    val isLoading: Boolean = true,
    val collection: AchievementCollection? = null,
    val hasError: Boolean = false,
    val sessionRequired: Boolean = false
)
