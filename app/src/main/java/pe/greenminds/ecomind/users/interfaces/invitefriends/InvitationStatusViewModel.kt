package pe.greenminds.ecomind.users.interfaces.invitefriends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.users.application.GetInvitationStatusUseCase
import javax.inject.Inject

@HiltViewModel
class InvitationStatusViewModel @Inject constructor(
    private val getInvitationStatus: GetInvitationStatusUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(InvitationStatusUiState())
    val state: StateFlow<InvitationStatusUiState> = _state.asStateFlow()

    // The id comes from the route; the screen calls this when it is shown and on "Try again"
    fun loadStatus(friendshipId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, loadFailed = false) }

            getInvitationStatus(friendshipId)
                .onSuccess { invitation ->
                    _state.update { it.copy(isLoading = false, invitation = invitation) }
                }
                .onFailure {
                    _state.update { it.copy(isLoading = false, loadFailed = true) }
                }
        }
    }
}
