package pe.greenminds.ecomind.community.presentation.states

import pe.greenminds.ecomind.community.domain.entity.Community
import pe.greenminds.ecomind.community.domain.entity.CommunityEvent

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
    val errorMessage: String? = null
) {
    val filteredLocalCommunities: List<Community>
        get() {
            val query = searchQuery.trim()
            if (query.isEmpty()) return localCommunities

            return localCommunities.filter {
                it.name.contains(query, ignoreCase = true)
            }
        }
}