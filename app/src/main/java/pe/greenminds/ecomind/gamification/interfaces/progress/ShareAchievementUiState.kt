package pe.greenminds.ecomind.gamification.interfaces.progress

import pe.greenminds.ecomind.gamification.domain.model.AchievementGroup
import pe.greenminds.ecomind.gamification.domain.model.AchievementShare

data class ShareAchievementUiState(
    val isLoading: Boolean = true,
    val isSharing: Boolean = false,
    val communities: List<AchievementGroup> = emptyList(),
    val selectedCommunityId: Long? = null,
    val share: AchievementShare? = null,
    val hasError: Boolean = false,
    val sessionRequired: Boolean = false,
    val isSimulated: Boolean = false,
    val isSubmitted: Boolean = false
)
