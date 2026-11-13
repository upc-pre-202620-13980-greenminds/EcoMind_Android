package pe.greenminds.ecomind.users.interfaces.friendrequests

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
import pe.greenminds.ecomind.users.application.GetFriendRequestsUseCase
import pe.greenminds.ecomind.users.application.RespondFriendRequestUseCase
import javax.inject.Inject

@HiltViewModel
class FriendRequestsViewModel @Inject constructor(
    private val getFriendRequests: GetFriendRequestsUseCase,
    private val respondFriendRequest: RespondFriendRequestUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(FriendRequestsUiState())
    val state: StateFlow<FriendRequestsUiState> = _state.asStateFlow()

    // Tells the screen that a request was answered, so the previous screens load again
    private val _friendsChanged = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val friendsChanged: SharedFlow<Unit> = _friendsChanged.asSharedFlow()

    init {
        loadRequests()
    }

    // Also used by "Try again"
    fun loadRequests() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, loadFailed = false) }

            getFriendRequests()
                .onSuccess { requests ->
                    _state.update { it.copy(isLoading = false, requests = requests) }
                }
                .onFailure {
                    _state.update { it.copy(isLoading = false, loadFailed = true) }
                }
        }
    }

    fun accept(friendshipId: Long) = answer(friendshipId, accepted = true)

    fun decline(friendshipId: Long) = answer(friendshipId, accepted = false)

    private fun answer(friendshipId: Long, accepted: Boolean) {
        if (_state.value.answeringId != null) return

        viewModelScope.launch {
            _state.update {
                it.copy(answeringId = friendshipId, errorFriendshipId = null, errorMessage = null)
            }

            respondFriendRequest(friendshipId, accepted)
                .onSuccess {
                    // The answered request leaves the "Received" list
                    _state.update { currentState ->
                        currentState.copy(
                            answeringId = null,
                            requests = currentState.requests?.let { requests ->
                                requests.copy(
                                    received = requests.received.filter { it.friendshipId != friendshipId }
                                )
                            }
                        )
                    }
                    _friendsChanged.tryEmit(Unit)
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            answeringId = null,
                            errorFriendshipId = friendshipId,
                            errorMessage = messageFor(exception)
                        )
                    }
                }
        }
    }
}
