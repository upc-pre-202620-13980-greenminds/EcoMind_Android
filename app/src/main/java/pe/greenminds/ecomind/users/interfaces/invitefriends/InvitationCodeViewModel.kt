package pe.greenminds.ecomind.users.interfaces.invitefriends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.users.application.GetMyInvitationCodeUseCase
import javax.inject.Inject

@HiltViewModel
class InvitationCodeViewModel @Inject constructor(
    private val getMyInvitationCode: GetMyInvitationCodeUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(InvitationCodeUiState())
    val state: StateFlow<InvitationCodeUiState> = _state.asStateFlow()

    init {
        loadCode()
    }

    // Also used by "Try again"
    fun loadCode() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, loadFailed = false) }

            getMyInvitationCode()
                .onSuccess { code ->
                    _state.update { it.copy(isLoading = false, code = code) }
                }
                .onFailure {
                    _state.update { it.copy(isLoading = false, loadFailed = true) }
                }
        }
    }

    // The screen copies the text; the state only shows the confirmation
    fun onCopied() {
        _state.update { it.copy(isCopied = true) }
    }
}
