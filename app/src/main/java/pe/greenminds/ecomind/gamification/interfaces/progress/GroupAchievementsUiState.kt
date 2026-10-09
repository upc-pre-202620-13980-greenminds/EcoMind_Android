package pe.greenminds.ecomind.gamification.interfaces.progress

import pe.greenminds.ecomind.gamification.domain.model.AchievementCollection

data class GroupAchievementsUiState(
    val isLoading: Boolean = true,
    val hasError: Boolean = false,
    val sessionRequired: Boolean = false,
    val collection: AchievementCollection? = null
)
