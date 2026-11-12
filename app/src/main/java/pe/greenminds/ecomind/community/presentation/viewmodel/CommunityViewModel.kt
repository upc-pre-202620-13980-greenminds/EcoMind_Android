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
import java.io.IOException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import pe.greenminds.ecomind.community.application.usecase.GetCommunityAchievementsUseCase
import pe.greenminds.ecomind.community.application.usecase.GetCommunityGoalsUseCase
import pe.greenminds.ecomind.community.application.usecase.GetCurrentLocalityUseCase
import pe.greenminds.ecomind.community.application.usecase.LeaveCommunityUseCase
import pe.greenminds.ecomind.community.domain.valueobjects.LocalityException
import pe.greenminds.ecomind.community.domain.valueobjects.LocalityFailure
@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val getEvents: GetCommunityEventsUseCase,
    private val getMemberships: GetMyCommunityMembershipsUseCase,
    private val getLocalCommunities: GetLocalCommunitiesUseCase,
    private val getTopicCommunities: GetTopicCommunitiesUseCase,
    private val joinCommunity: JoinCommunityUseCase,
    private val getPreferredLocality: GetPreferredLocalityUseCase,
    private val savePreferredLocality: SavePreferredLocalityUseCase,
    private val getCurrentLocality: GetCurrentLocalityUseCase,
    private val leaveCommunity: LeaveCommunityUseCase,
    private val getAchievements: GetCommunityAchievementsUseCase,
    private val getGoals: GetCommunityGoalsUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CommunityUiState())
    val state: StateFlow<CommunityUiState> = _state.asStateFlow()
    private var localityJob: Job? = null
    private var localSearchJob: Job? = null
    private var localityRequestId: Long = 0L

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
        if (!_state.value.canAccessMoreCommunities) return

        cancelLocalityDetection()
        localSearchJob?.cancel()
        localSearchJob = null

        _state.update {
            it.copy(
                activeDirectoryTab = CommunityDirectoryTab.MORE,
                isShowingMoreCommunitiesList = true,
                topicListMode = TopicCommunityListMode.MEMBERS,
                searchQuery = "",
                isLoadingLocalCommunities = false,
                errorMessage = null,
                errorMessageRes = null
            )
        }

        loadTopicCommunities()
    }

    fun showLocalCommunity() {
        _state.update {
            it.copy(
                activeDirectoryTab = CommunityDirectoryTab.LOCAL,
                isShowingMoreCommunitiesList = false,
                selectedCommunity = it.selectedLocalCommunity,
                searchQuery = "",
                errorMessage = null,
                errorMessageRes = null
            )
        }

        if (_state.value.selectedLocalCommunity == null) {
            searchCurrentLocality()
        }
    }

    fun showTopicDiscovery() {
        if (!_state.value.canAccessMoreCommunities) return
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
        if (community.type == CommunityType.TOPIC &&
            !_state.value.canAccessMoreCommunities
        ) {
            return
        }
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
        if (community.id !in _state.value.membershipCommunityIds) return
        _state.update {
            it.copy(
                selectedCommunity = community,
                activeDirectoryTab = CommunityDirectoryTab.MORE,
                isShowingMoreCommunitiesList = false,
                errorMessage = null,
                errorMessageRes = null
            )
        }
        loadCommunityContent(community)
    }

    fun saveLocalityAndSearch() {
        val locality = _state.value.localityInput.trim()

        if (locality.isBlank()) {
            _state.update {
                it.copy(localityErrorRes = R.string.community_enter_location_error)
            }
            return
        }

        cancelLocalityDetection()
        localSearchJob?.cancel()

        localSearchJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    hasRequestedLocation = true,
                    isResolvingLocality = false,
                    preferredLocality = locality,
                    localityInput = locality,
                    localCommunities = emptyList(),
                    searchQuery = "",
                    localityErrorRes = null,
                    errorMessage = null,
                    errorMessageRes = null
                )
            }

            try {
                savePreferredLocality(locality)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: IOException) {
                _state.update {
                    it.copy(
                        localityErrorRes = R.string.community_locality_save_error
                    )
                }
            }

            loadLocalCommunities(locality)
        }
    }

    fun retry() {
        when {
            _state.value.isShowingMoreCommunitiesList ->
                loadTopicCommunities()

            _state.value.selectedCommunity != null ->
                loadCommunityContent(checkNotNull(_state.value.selectedCommunity))

            _state.value.preferredLocality != null ->
                searchCurrentLocality()

            else ->
                loadEntry()
        }
    }
    fun joinPendingCommunity() {

        val community = _state.value.pendingCommunityDetails ?: return
        if (community.type == CommunityType.TOPIC &&
            !_state.value.canAccessMoreCommunities
        ) {
            return
        }
        if (_state.value.joiningCommunityId != null) return

        viewModelScope.launch {
            _state.update {
                it.copy(joiningCommunityId = community.id, errorMessage = null, errorMessageRes = null)
            }

            joinCommunity(community.id)
                .onSuccess { membership ->
                    _state.update {
                        it.copy(
                            membershipCommunityIds =
                                it.membershipCommunityIds + membership.communityId
                        )
                    }
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
                        loadCommunityContent(community)
                    } else {
                        _state.update { currentState ->
                            currentState.copy(
                                membershipCommunityIds =
                                    currentState.membershipCommunityIds + membership.communityId,
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
                                membershipCommunityIds = memberIds,
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
                                membershipCommunityIds = memberIds,
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
                            membershipCommunityIds = memberships
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
                                loadSelection()
                            }
                        }
                }
        }
    }

    private suspend fun loadSelection() {
        val locality = try {
            getPreferredLocality()?.trim()?.takeIf(String::isNotEmpty)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: IOException) {
            null
        }

        _state.update {
            it.copy(
                isLoadingCommunity = false,
                selectedCommunity = null,
                selectedLocalCommunity = null,
                preferredLocality = locality,
                localityInput = locality.orEmpty(),
                localCommunities = emptyList(),
                errorMessage = null,
                errorMessageRes = null
            )
        }

        if (locality != null) {
            searchLocalCommunities(locality)
        }
    }

    private suspend fun loadLocalCommunities(locality: String) {
        _state.update {
            it.copy(
                isLoadingLocalCommunities = true,
                localCommunities = emptyList(),
                errorMessage = null,
                errorMessageRes = null
            )
        }

        getLocalCommunities(locality)
            .onSuccess { communities ->
                _state.update {
                    it.copy(
                        isLoadingLocalCommunities = false,
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
                        isLoadingLocalCommunities = false,
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

    private fun loadTopicCommunities() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingTopicCommunities = true) }

            getMemberships()
                .onSuccess { memberships ->
                    _state.update {
                        it.copy(
                            membershipCommunityIds = memberships
                                .map { membership -> membership.communityId }
                                .toSet()
                        )
                    }
                    if (memberships.isEmpty()) {
                        _state.update {
                            it.copy(
                                activeDirectoryTab = CommunityDirectoryTab.LOCAL,
                                isShowingMoreCommunitiesList = false,
                                selectedCommunity = null,
                                selectedLocalCommunity = null,
                                topicCommunities = emptyList(),
                                isLoadingTopicCommunities = false,
                                searchQuery = ""
                            )
                        }
                        loadSelection()
                        return@onSuccess
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
        loadCommunityContent(community)
    }
    private fun loadEvents(communityId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingEvents = true) }

            getEvents(communityId)
                .onSuccess { events ->
                    _state.update {
                        it.copy(events = events, isLoadingEvents = false)
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoadingEvents = false,
                            errorMessage = error.message,
                            errorMessageRes = if (error.message == null) {
                                R.string.community_events_error
                            } else null
                        )
                    }
                }
        }
    }

    private fun loadAchievements(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingAchievements = true) }

            getAchievements(id)
                .onSuccess { achievements ->
                    _state.update {
                        it.copy(achievements = achievements, isLoadingAchievements = false)
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoadingAchievements = false,
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
    }

    private fun loadGoals(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingGoals = true) }

            getGoals(id)
                .onSuccess { goals ->
                    _state.update {
                        it.copy(goals = goals, isLoadingGoals = false)
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoadingGoals = false,
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
    }

    fun markLocationRequested() {
        _state.update {
            it.copy(hasRequestedLocation = true)
        }
    }

    fun onLocationPermissionDenied() {
        _state.update {
            it.copy(
                hasRequestedLocation = true,
                isResolvingLocality = false,
                localityErrorRes = R.string.community_location_permission_denied
            )
        }
    }

    fun detectCurrentLocality() {
        cancelLocalityDetection()
        localSearchJob?.cancel()

        val requestId = localityRequestId

        localityJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    hasRequestedLocation = true,
                    isResolvingLocality = true,
                    isLoadingLocalCommunities = false,
                    localityErrorRes = null,
                    errorMessage = null,
                    errorMessageRes = null
                )
            }

            try {
                getCurrentLocality()
                    .onSuccess { locality ->
                        if (requestId != localityRequestId) return@onSuccess

                        _state.update {
                            it.copy(
                                preferredLocality = locality,
                                localityInput = locality,
                                searchQuery = "",
                                localCommunities = emptyList(),
                                localityErrorRes = null
                            )
                        }

                        searchLocalCommunities(locality)
                    }
                    .onFailure { error ->
                        if (requestId != localityRequestId) return@onFailure

                        val message = when ((error as? LocalityException)?.reason) {
                            LocalityFailure.PERMISSION_DENIED ->
                                R.string.community_location_permission_denied

                            LocalityFailure.LOCATION_DISABLED ->
                                R.string.community_location_disabled

                            LocalityFailure.GEOCODING_UNAVAILABLE ->
                                R.string.community_geocoding_unavailable

                            else ->
                                R.string.community_location_unavailable
                        }

                        _state.update {
                            it.copy(localityErrorRes = message)
                        }
                    }
            } finally {
                if (requestId == localityRequestId) {
                    _state.update {
                        it.copy(isResolvingLocality = false)
                    }
                }
            }
        }
    }

    fun cancelLocalityDetection() {
        localityRequestId++
        localityJob?.cancel()
        localityJob = null

        _state.update {
            it.copy(
                isResolvingLocality = false,
                hasRequestedLocation = false
            )
        }
    }

    fun searchCurrentLocality() {
        val locality = _state.value.preferredLocality
            ?.takeIf(String::isNotBlank)
            ?: return

        searchLocalCommunities(locality)
    }

    private fun searchLocalCommunities(locality: String) {
        localSearchJob?.cancel()
        localSearchJob = viewModelScope.launch {
            loadLocalCommunities(locality)
        }
    }

    fun toggleCommunityOptions() {
        _state.update {
            it.copy(showCommunityOptions = !it.showCommunityOptions)
        }
    }

    fun dismissCommunityOptions() {
        _state.update {
            it.copy(showCommunityOptions = false)
        }
    }

    fun requestLeaveCommunity() {
        _state.update {
            it.copy(
                showCommunityOptions = false,
                showLeaveConfirmation = true,
                errorMessage = null,
                errorMessageRes = null
            )
        }
    }

    fun dismissLeaveConfirmation() {
        if (_state.value.isLeavingCommunity) return

        _state.update {
            it.copy(showLeaveConfirmation = false)
        }
    }

    fun confirmLeaveCommunity() {
        val community = _state.value.selectedCommunity ?: return
        if (_state.value.isLeavingCommunity) return

        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLeavingCommunity = true,
                    errorMessage = null,
                    errorMessageRes = null
                )
            }

            leaveCommunity(community.id)
                .onSuccess {
                    _state.update { current ->
                        current.copy(
                            membershipCommunityIds =
                                current.membershipCommunityIds - community.id,
                            selectedCommunity = null,
                            selectedLocalCommunity = null,
                            showLeaveConfirmation = false,
                            isLeavingCommunity = false,
                            isShowingMoreCommunitiesList =
                                community.type == CommunityType.TOPIC,
                            activeDirectoryTab =
                                if (community.type == CommunityType.TOPIC) {
                                    CommunityDirectoryTab.MORE
                                } else {
                                    CommunityDirectoryTab.LOCAL
                                },
                            errorMessage = null,
                            errorMessageRes = null
                        )
                    }

                    if (community.type == CommunityType.TOPIC) {
                        loadTopicCommunities()
                    } else {
                        loadSelection()
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLeavingCommunity = false,
                            errorMessage = error.message,
                            errorMessageRes = if (error.message == null) {
                                R.string.community_leave_error
                            } else {
                                null
                            }
                        )
                    }
                }
        }
    }

    private fun loadCommunityContent(community: Community) {
        loadEvents(community.id)
        loadAchievements(community.id)
        loadGoals(community.id)
    }
    fun refreshCommunityContent() {
        val community = _state.value.selectedCommunity ?: return
        loadCommunityContent(community)
    }

    fun onCommunityCreated(community: Community) {
        _state.update {
            it.copy(
                membershipCommunityIds = it.membershipCommunityIds + community.id,
                topicCommunities = it.topicCommunities
                    .filterNot { existing -> existing.id == community.id } + community,
                selectedCommunity = community,
                activeDirectoryTab = CommunityDirectoryTab.MORE,
                isShowingMoreCommunitiesList = false,
                topicListMode = TopicCommunityListMode.MEMBERS,
                selectedSection = CommunitySection.EVENTS,
                searchQuery = "",
                pendingCommunityDetails = null,
                errorMessage = null,
                errorMessageRes = null
            )
        }

        loadCommunityContent(community)
    }
}
