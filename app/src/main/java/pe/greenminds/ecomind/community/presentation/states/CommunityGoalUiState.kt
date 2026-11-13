package pe.greenminds.ecomind.community.presentation.states

import pe.greenminds.ecomind.community.domain.entity.CommunityGoalPanel

data class CommunityGoalUiState(
    val communityId: Long? = null,
    val panel: CommunityGoalPanel? = null,
    val loading: Boolean = false,
    val saving: Boolean = false,
    val expanded: Boolean = false,
    val showCreate: Boolean = false,
    val topic: String = "water",
    val error: String? = null,
    val errorRes: Int? = null
)