package pe.greenminds.ecomind.gamification.interfaces.achievements

import pe.greenminds.ecomind.gamification.domain.model.AchievementDetail

sealed class AchievementDetailUiState {
    data object Loading : AchievementDetailUiState()
    data class Success(val detail: AchievementDetail) : AchievementDetailUiState()
    data object Error : AchievementDetailUiState()
    data object SessionRequired : AchievementDetailUiState()
    data object NotFound : AchievementDetailUiState()
}
