package pe.greenminds.ecomind.community.presentation.states

import pe.greenminds.ecomind.community.domain.entity.Community
import pe.greenminds.ecomind.community.domain.entity.CommunityAchievement
import pe.greenminds.ecomind.community.domain.entity.CommunityEvent
import pe.greenminds.ecomind.community.domain.entity.CommunityGoal

data class CommunityUiState(
    val isLoadingEvents: Boolean = false,
    val events: List<CommunityEvent> = emptyList(),
    val selectedSection: CommunitySection = CommunitySection.EVENTS,

    val isLoadingCommunity: Boolean = true,
    val preferredLocality: String? = null,
    val localityInput: String = "",
    val localCommunities: List<Community> = emptyList(),
    val searchQuery: String = "",

    val pendingCommunityDetails: Community? = null,
    val joiningCommunityId: Long? = null,
    val selectedCommunity: Community? = null,
    val errorMessage: String? = null,
    val errorMessageRes: Int? = null,

    val activeDirectoryTab: CommunityDirectoryTab = CommunityDirectoryTab.LOCAL,
    val isShowingMoreCommunitiesList: Boolean = false,
    val selectedLocalCommunity: Community? = null,
    val topicCommunities: List<Community> = emptyList(),
    val membershipCommunityIds: Set<Long> = emptySet(),
    val topicListMode: TopicCommunityListMode = TopicCommunityListMode.MEMBERS,
    val isLoadingTopicCommunities: Boolean = false,

    val isLoadingLocalCommunities: Boolean = false,
    val isResolvingLocality: Boolean = false,
    val hasRequestedLocation: Boolean = false,
    val localityErrorRes: Int? = null,

    val showCommunityOptions: Boolean = false,
    val showLeaveConfirmation: Boolean = false,
    val isLeavingCommunity: Boolean = false,

    val achievements: List<CommunityAchievement> = emptyList(),

    val isLoadingAchievements: Boolean = false
) {
    val filteredLocalCommunities: List<Community>
        get() {
            val query = searchQuery.trim()
            if (query.isEmpty()) return localCommunities

            return localCommunities.filter {
                it.name.contains(query, ignoreCase = true)
        }
    }

    val visibleTopicCommunities: List<Community>
        get() {
            val matchingCommunities = topicCommunities.filter { community ->
                val isMember = community.id in membershipCommunityIds

                when (topicListMode) {
                    TopicCommunityListMode.MEMBERS -> isMember
                    TopicCommunityListMode.DISCOVER -> !isMember
                }
            }

            val query = searchQuery.trim()
            if (query.isEmpty()) return matchingCommunities

            return matchingCommunities.filter { community ->
                community.name.contains(query, ignoreCase = true)
            }
        }

    val canAccessMoreCommunities: Boolean
        get() = membershipCommunityIds.isNotEmpty()
}
