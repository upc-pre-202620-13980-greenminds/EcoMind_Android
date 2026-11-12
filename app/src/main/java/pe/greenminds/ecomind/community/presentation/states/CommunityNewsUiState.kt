package pe.greenminds.ecomind.community.presentation.states

import pe.greenminds.ecomind.community.domain.entity.CommunityPost
import pe.greenminds.ecomind.community.domain.entity.PostReactions

data class CommunityNewsUiState(
    val communityId: Long? = null,
    val posts: List<CommunityPost> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val errorRes: Int? = null,
    val selectedPostId: Long? = null,
    val showPicker: Boolean = false,
    val reactions: PostReactions? = null,
    val isLoadingReactions: Boolean = false,
    val isSavingReaction: Boolean = false,
    val reactionError: String? = null,
    val reactionErrorRes: Int? = null
)