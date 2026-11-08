package pe.greenminds.ecomind.community.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import pe.greenminds.ecomind.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.community.application.usecase.GetCommunityEventsUseCase
import pe.greenminds.ecomind.community.application.usecase.GetLocalCommunitiesUseCase
import pe.greenminds.ecomind.community.application.usecase.GetMyCommunityMembershipsUseCase
import pe.greenminds.ecomind.community.application.usecase.GetTopicCommunitiesUseCase
import pe.greenminds.ecomind.community.application.usecase.GetPreferredLocalityUseCase
import pe.greenminds.ecomind.community.application.usecase.JoinCommunityUseCase
import pe.greenminds.ecomind.community.application.usecase.SavePreferredLocalityUseCase
import pe.greenminds.ecomind.community.domain.entity.Community
import pe.greenminds.ecomind.community.domain.valueobjects.CommunityType
import pe.greenminds.ecomind.community.presentation.states.CommunityDirectoryTab
import pe.greenminds.ecomind.community.presentation.states.CommunitySection
import pe.greenminds.ecomind.community.presentation.states.CommunityUiState
import pe.greenminds.ecomind.community.presentation.states.TopicCommunityListMode

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val getEvents: GetCommunityEventsUseCase,
    private val getMemberships: GetMyCommunityMembershipsUseCase,
    private val getLocalCommunities: GetLocalCommunitiesUseCase,
    private val getTopicCommunities: GetTopicCommunitiesUseCase,
    private val joinCommunity: JoinCommunityUseCase,
    private val getPreferredLocality: GetPreferredLocalityUseCase,
    private val savePreferredLocality: SavePreferredLocalityUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CommunityUiState())
    val state: StateFlow<CommunityUiState> = _state.asStateFlow()

    init {
        loadEntry()
    }

    fun selectSection(section: CommunitySection) {
        _state.update { it.copy(selectedSection = section) }
    }

    fun updateLocalityInput(value: String) {
        _state.update { it.copy(localityInput = value) }
    }

    fun updateSearchQuery(value: String) {
        _state.update { it.copy(searchQuery = value) }
    }

    fun showMoreCommunities() {
        _state.update {
            it.copy(
                activeDirectoryTab = CommunityDirectoryTab.MORE,
                isShowingMoreCommunitiesList = true,
                topicListMode = TopicCommunityListMode.MEMBERS,
                searchQuery = ""
            )
        }
        loadTopicCommunities()
    }

    fun showLocalCommunity() {
        _state.update {
            it.copy(
                activeDirectoryTab = CommunityDirectoryTab.LOCAL,
                isShowingMoreCommunitiesList = false,
                selectedCommunity = it.selectedLocalCommunity
            )
        }
    }

    fun showTopicDiscovery() {
        _state.update {
            it.copy(
                topicListMode = TopicCommunityListMode.DISCOVER,
                searchQuery = ""
            )
        }
    }

    fun showMyTopicCommunities() {
        _state.update {
            it.copy(
                topicListMode = TopicCommunityListMode.MEMBERS,
                searchQuery = ""
            )
        }
    }
    fun showCommunityDetails(community: Community) {
        _state.update {
            it.copy(pendingCommunityDetails = community, errorMessage = null, errorMessageRes = null)
        }
    }

    fun dismissCommunityDetails() {
        if (_state.value.joiningCommunityId == null) {
            _state.update {
                it.copy(pendingCommunityDetails = null, errorMessage = null, errorMessageRes = null)
            }
        }
    }

    fun enterTopicCommunity(community: Community) {
        _state.update {
            it.copy(
                selectedCommunity = community,
                activeDirectoryTab = CommunityDirectoryTab.MORE,
                isShowingMoreCommunitiesList = false,
                errorMessage = null,
                errorMessageRes = null
            )
        }
        loadEvents()
    }

    fun saveLocalityAndSearch() {
        val locality = _state.value.localityInput.trim()
        if (locality.isBlank()) {
            _state.update {
                it.copy(errorMessage = null, errorMessageRes = R.string.community_enter_location_error)
            }
            return
        }

        viewModelScope.launch {
            savePreferredLocality(locality)
            _state.update {
                it.copy(preferredLocality = locality, errorMessage = null, errorMessageRes = null)
            }
            loadLocalCommunities(locality)
        }
    }

    fun retry() {
        when {
            _state.value.isShowingMoreCommunitiesList -> {
                loadTopicCommunities()
            }

            _state.value.preferredLocality != null -> {
                val locality = _state.value.preferredLocality.orEmpty()

                viewModelScope.launch {
                    loadLocalCommunities(locality)
                }
            }

            else -> {
                loadEntry()
            }
        }
    }
    fun joinPendingCommunity() {
        val community = _state.value.pendingCommunityDetails ?: return
        if (_state.value.joiningCommunityId != null) return

        viewModelScope.launch {
            _state.update {
                it.copy(joiningCommunityId = community.id, errorMessage = null, errorMessageRes = null)
            }

            joinCommunity(community.id)
                .onSuccess { membership ->
                    if (community.type == CommunityType.LOCAL) {
                        _state.update {
                            it.copy(
                                selectedCommunity = community,
                                selectedLocalCommunity = community,
                                activeDirectoryTab = CommunityDirectoryTab.LOCAL,
                                isShowingMoreCommunitiesList = false,
                                pendingCommunityDetails = null,
                                joiningCommunityId = null,
                                isLoadingCommunity = false,
                                errorMessage = null,
                                errorMessageRes = null
                            )
                        }
                        loadEvents()
                    } else {
                        _state.update { currentState ->
                            currentState.copy(
                                topicMembershipIds =
                                    currentState.topicMembershipIds + membership.communityId,
                                activeDirectoryTab = CommunityDirectoryTab.MORE,
                                isShowingMoreCommunitiesList = true,
                                topicListMode = TopicCommunityListMode.MEMBERS,
                                pendingCommunityDetails = null,
                                joiningCommunityId = null,
                                errorMessage = null,
                                errorMessageRes = null
                            )
                        }
                    }
                }
                .onFailure { error ->
                    handleJoinFailure(community, error)
                }
        }
    }

    private fun handleJoinFailure(community: Community, error: Throwable) {
        if (community.type != CommunityType.TOPIC) {
            _state.update {
                it.copy(
                    joiningCommunityId = null,
                    errorMessage = error.message,
                    errorMessageRes = if (error.message == null) {
                        R.string.community_join_error
                    } else {
                        null
                    }
                )
            }
            return
        }

        viewModelScope.launch {
            getMemberships()
                .onSuccess { memberships ->
                    val memberIds = memberships.map { it.communityId }.toSet()

                    _state.update { currentState ->
                        if (community.id in memberIds) {
                            currentState.copy(
                                topicMembershipIds = memberIds,
                                activeDirectoryTab = CommunityDirectoryTab.MORE,
                                isShowingMoreCommunitiesList = true,
                                topicListMode = TopicCommunityListMode.MEMBERS,
                                pendingCommunityDetails = null,
                                joiningCommunityId = null,
                                errorMessage = null,
                                errorMessageRes = null
                            )
                        } else {
                            currentState.copy(
                                topicMembershipIds = memberIds,
                                joiningCommunityId = null,
                                errorMessage = error.message,
                                errorMessageRes = if (error.message == null) {
                                    R.string.community_join_error
                                } else {
                                    null
                                }
                            )
                        }
                    }
                }
                .onFailure {
                    _state.update { currentState ->
                        currentState.copy(
                            joiningCommunityId = null,
                            errorMessage = error.message,
                            errorMessageRes = if (error.message == null) {
                                R.string.community_join_error
                            } else {
                                null
                            }
                        )
                    }
                }
        }
    }

    private fun loadEntry() {
        viewModelScope.launch {
            _state.update {
                it.copy(isLoadingCommunity = true, errorMessage = null, errorMessageRes = null)
            }

            getMemberships()
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoadingCommunity = false,
                            errorMessage = error.message,
                            errorMessageRes = if (error.message == null) R.string.community_load_error else null
                        )
                    }
                }
                .onSuccess { memberships ->
                    _state.update {
                        it.copy(
                            topicMembershipIds = memberships
                                .map { membership -> membership.communityId }
                                .toSet()
                        )
                    }
                    getLocalCommunities(null)
                        .onFailure { error ->
                            _state.update {
                                it.copy(
                                    isLoadingCommunity = false,
                                    errorMessage = error.message,
                                    errorMessageRes = if (error.message == null) {
                                        R.string.community_load_list_error
                                    } else {
                                        null
                                    }
                                )
                            }
                        }
                        .onSuccess { locals ->
                            val localMembership = memberships.firstOrNull { membership ->
                                locals.any { it.id == membership.communityId }
                            }

                            if (localMembership != null) {
                                val community = locals.first {
                                    it.id == localMembership.communityId
                                }
                                selectCommunity(community)
                            } else {
                                loadSelection(locals)
                            }
                        }
                }
        }
    }

    private suspend fun loadSelection(allLocals: List<Community>) {
        val locality = getPreferredLocality()

        if (locality.isNullOrBlank()) {
            _state.update {
                it.copy(
                    isLoadingCommunity = false,
                    localCommunities = emptyList(),
                    preferredLocality = null,
                    errorMessage = null,
                    errorMessageRes = null
                )
            }
            return
        }

        _state.update { it.copy(preferredLocality = locality) }

        if (allLocals.all { it.locality.equals(locality, ignoreCase = true) }) {
            _state.update {
                it.copy(
                    isLoadingCommunity = false,
                    localCommunities = allLocals,
                    errorMessage = null,
                    errorMessageRes = null
                )
            }
        } else {
            loadLocalCommunities(locality)
        }
    }

    private suspend fun loadLocalCommunities(locality: String) {
        _state.update {
            it.copy(isLoadingCommunity = true, errorMessage = null, errorMessageRes = null)
        }

        getLocalCommunities(locality)
            .onSuccess { communities ->
                _state.update {
                    it.copy(
                        isLoadingCommunity = false,
                        preferredLocality = locality,
                        localCommunities = communities,
                        errorMessage = null,
                        errorMessageRes = null
                    )
                }
            }
            .onFailure { error ->
                _state.update {
                    it.copy(
                        isLoadingCommunity = false,
                        errorMessage = error.message,
                        errorMessageRes = if (error.message == null) {
                            R.string.community_location_error
                        } else {
                            null
                        }
                    )
                }
            }
    }

    private fun loadTopicCommunities() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingTopicCommunities = true) }

            getMemberships()
                .onSuccess { memberships ->
                    _state.update {
                        it.copy(
                            topicMembershipIds = memberships
                                .map { membership -> membership.communityId }
                                .toSet()
                        )
                    }

                    getTopicCommunities()
                        .onSuccess { communities ->
                            _state.update {
                                it.copy(
                                    topicCommunities = communities.filter { community ->
                                        community.type == CommunityType.TOPIC
                                    },
                                    isLoadingTopicCommunities = false,
                                    errorMessage = null,
                                    errorMessageRes = null
                                )
                            }
                        }
                        .onFailure { error ->
                            _state.update {
                                it.copy(
                                    isLoadingTopicCommunities = false,
                                    errorMessage = error.message,
                                    errorMessageRes = if (error.message == null) {
                                        R.string.community_load_list_error
                                    } else {
                                        null
                                    }
                                )
                            }
                        }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoadingTopicCommunities = false,
                            errorMessage = error.message,
                            errorMessageRes = if (error.message == null) {
                                R.string.community_load_error
                            } else {
                                null
                            }
                        )
                    }
                }
        }
    }
    private fun selectCommunity(community: Community) {
        _state.update {
            it.copy(
                isLoadingCommunity = false,
                selectedCommunity = community,
                selectedLocalCommunity =
                    if (community.type == CommunityType.LOCAL) community
                    else it.selectedLocalCommunity,
                activeDirectoryTab = CommunityDirectoryTab.LOCAL,
                isShowingMoreCommunitiesList = false,
                errorMessage = null,
                errorMessageRes = null
            )
        }
        loadEvents()
    }
    private fun loadEvents() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingEvents = true) }

            getEvents()
                .onSuccess { events ->
                    _state.update {
                        it.copy(isLoadingEvents = false, events = events)
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoadingEvents = false,
                            errorMessage = error.message,
                            errorMessageRes = if (error.message == null) {
                                R.string.community_events_error
                            } else {
                                null
                            }
                        )
                    }
                }
        }
    }
}
