package pe.greenminds.ecomind.community.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.community.application.usecase.GetCommunityEventsUseCase
import pe.greenminds.ecomind.community.application.usecase.GetLocalCommunitiesUseCase
import pe.greenminds.ecomind.community.application.usecase.GetMyCommunityMembershipsUseCase
import pe.greenminds.ecomind.community.application.usecase.GetPreferredLocalityUseCase
import pe.greenminds.ecomind.community.application.usecase.JoinCommunityUseCase
import pe.greenminds.ecomind.community.application.usecase.SavePreferredLocalityUseCase
import pe.greenminds.ecomind.community.domain.entity.Community
import pe.greenminds.ecomind.community.domain.valueobjects.CommunityType
import pe.greenminds.ecomind.community.presentation.states.CommunitySection
import pe.greenminds.ecomind.community.presentation.states.CommunityUiState

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val getEvents: GetCommunityEventsUseCase,
    private val getMemberships: GetMyCommunityMembershipsUseCase,
    private val getLocalCommunities: GetLocalCommunitiesUseCase,
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

    fun showCommunityDetails(community: Community) {
        _state.update {
            it.copy(pendingCommunityDetails = community, errorMessage = null)
        }
    }

    fun dismissCommunityDetails() {
        if (_state.value.joiningCommunityId == null) {
            _state.update {
                it.copy(pendingCommunityDetails = null, errorMessage = null)
            }
        }
    }

    fun saveLocalityAndSearch() {
        val locality = _state.value.localityInput.trim()
        if (locality.isBlank()) {
            _state.update { it.copy(errorMessage = "Enter your location.") }
            return
        }

        viewModelScope.launch {
            savePreferredLocality(locality)
            _state.update {
                it.copy(preferredLocality = locality, errorMessage = null)
            }
            loadLocalCommunities(locality)
        }
    }

    fun retry() {
        loadEntry()
    }

    fun joinPendingCommunity() {
        val community = _state.value.pendingCommunityDetails ?: return
        if (_state.value.joiningCommunityId != null) return

        viewModelScope.launch {
            _state.update {
                it.copy(joiningCommunityId = community.id, errorMessage = null)
            }

            joinCommunity(community.id)
                .onSuccess {
                    _state.update {
                        it.copy(
                            selectedCommunity = community,
                            pendingCommunityDetails = null,
                            joiningCommunityId = null,
                            isLoadingCommunity = false,
                            errorMessage = null
                        )
                    }
                    loadEvents()
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            joiningCommunityId = null,
                            errorMessage = error.message ?: "It could not be joined."
                        )
                    }
                }
        }
    }

    private fun loadEntry() {
        viewModelScope.launch {
            _state.update {
                it.copy(isLoadingCommunity = true, errorMessage = null)
            }

            getMemberships()
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            isLoadingCommunity = false,
                            errorMessage = error.message ?: "Your community could not be loaded."
                        )
                    }
                }
                .onSuccess { memberships ->

                    getLocalCommunities(null)
                        .onFailure { error ->
                            _state.update {
                                it.copy(
                                    isLoadingCommunity = false,
                                    errorMessage = error.message
                                        ?: "The communities could not be loaded."
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
                    errorMessage = null
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
                    errorMessage = null
                )
            }
        } else {
            loadLocalCommunities(locality)
        }
    }

    private suspend fun loadLocalCommunities(locality: String) {
        _state.update { it.copy(isLoadingCommunity = true, errorMessage = null) }

        getLocalCommunities(locality)
            .onSuccess { communities ->
                _state.update {
                    it.copy(
                        isLoadingCommunity = false,
                        preferredLocality = locality,
                        localCommunities = communities,
                        errorMessage = null
                    )
                }
            }
            .onFailure { error ->
                _state.update {
                    it.copy(
                        isLoadingCommunity = false,
                        errorMessage = error.message ?: "The location could not be found."
                    )
                }
            }
    }

    private fun selectCommunity(community: Community) {
        _state.update {
            it.copy(
                isLoadingCommunity = false,
                selectedCommunity = community,
                errorMessage = null
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
                            errorMessage = error.message ?: "The events could not be loaded."
                        )
                    }
                }
        }
    }
}