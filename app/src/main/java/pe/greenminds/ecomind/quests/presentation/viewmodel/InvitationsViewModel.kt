package pe.greenminds.ecomind.quests.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.quests.application.usecase.GetCollabQuestInvitationUseCase
import pe.greenminds.ecomind.quests.presentation.states.InvitationsUiState
import javax.inject.Inject

@HiltViewModel
class InvitationsViewModel @Inject constructor(
    private val getInvitations: GetCollabQuestInvitationUseCase
): ViewModel() {

    private val _state = MutableStateFlow(InvitationsUiState())
    val state = _state.asStateFlow()
    private var loadJob: kotlinx.coroutines.Job? = null

    init {
        loadInvitations()
    }

    fun loadInvitations() = loadInvitations(showRefresh = false)

    fun refreshInvitations() = loadInvitations(showRefresh = true)

    private fun loadInvitations(showRefresh: Boolean) {
        if (_state.value.isLoading && loadJob?.isActive == true) return
        loadJob = viewModelScope.launch {
            _state.update { it.copy(isLoading = true, isRefreshing = showRefresh, hasError = false) }

            getInvitations()
                .onSuccess {
                    _state.value = InvitationsUiState(
                        invitations = it,
                        isLoading = false
                    )
                }
                .onFailure {
                    _state.update { it.copy(isLoading = false, isRefreshing = false, hasError = true) }
                }
        }
    }

}
