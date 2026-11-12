package pe.greenminds.ecomind.users.interfaces.invitefriends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.users.application.RedeemInvitationCodeUseCase
import pe.greenminds.ecomind.users.interfaces.friendrequests.messageFor
import javax.inject.Inject

@HiltViewModel
class InviteFriendsViewModel @Inject constructor(
    private val redeemInvitationCode: RedeemInvitationCodeUseCase
) : ViewModel() {

    companion object {
        // 8 characters plus the space of "K7MP 4QXZ" when the code is pasted
        private const val MAX_INPUT_LENGTH = 9
    }

    private val _state = MutableStateFlow(InviteFriendsUiState())
    val state: StateFlow<InviteFriendsUiState> = _state.asStateFlow()

    // Id of the request sent with the code, so the screen can open its status
    private val _requestSent = MutableSharedFlow<Long>(extraBufferCapacity = 1)
    val requestSent: SharedFlow<Long> = _requestSent.asSharedFlow()

    fun onCodeChange(code: String) {
        if (code.length > MAX_INPUT_LENGTH) return
        _state.update { it.copy(code = code.uppercase(), codeError = null) }
    }

    fun redeemCode() {
        if (_state.value.isSending) return

        viewModelScope.launch {
            _state.update { it.copy(isSending = true, codeError = null) }

            // The use case removes the space and checks the format before calling the web services
            redeemInvitationCode(_state.value.code)
                .onSuccess { friendship ->
                    _state.update { InviteFriendsUiState() }
                    _requestSent.tryEmit(friendship.id)
                }
                .onFailure { exception ->
                    _state.update { it.copy(isSending = false, codeError = messageFor(exception)) }
                }
        }
    }
}
