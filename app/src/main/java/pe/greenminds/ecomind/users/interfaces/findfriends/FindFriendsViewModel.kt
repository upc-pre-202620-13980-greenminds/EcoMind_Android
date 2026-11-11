package pe.greenminds.ecomind.users.interfaces.findfriends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.users.application.SearchUsersUseCase
import pe.greenminds.ecomind.users.application.SendFriendRequestUseCase
import pe.greenminds.ecomind.users.domain.model.FriendRelation
import pe.greenminds.ecomind.users.interfaces.friendrequests.messageFor
import javax.inject.Inject

@HiltViewModel
class FindFriendsViewModel @Inject constructor(
    private val searchUsers: SearchUsersUseCase,
    private val sendFriendRequest: SendFriendRequestUseCase
) : ViewModel() {

    companion object {
        // Waits for the user to stop typing before asking the web services again
        private const val SEARCH_DELAY_MILLIS = 300L
    }

    private val _state = MutableStateFlow(FindFriendsUiState())
    val state: StateFlow<FindFriendsUiState> = _state.asStateFlow()

    // Tells the screen that a request was sent, so Profile can load its friends again
    private val _friendsChanged = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val friendsChanged: SharedFlow<Unit> = _friendsChanged.asSharedFlow()

    private var searchJob: Job? = null

    init {
        search()
    }

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }

        // Each new letter cancels the search that was waiting
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DELAY_MILLIS)
            runSearch()
        }
    }

    // Also used by "Try again" and when coming back from the friend requests
    fun search() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch { runSearch() }
    }

    fun sendRequest(userId: Long) {
        if (_state.value.sendingToUserId != null) return

        viewModelScope.launch {
            _state.update {
                it.copy(sendingToUserId = userId, errorUserId = null, errorMessage = null)
            }

            sendFriendRequest(userId)
                .onSuccess {
                    // The row changes to "Sent" without searching again
                    _state.update { currentState ->
                        currentState.copy(
                            sendingToUserId = null,
                            results = currentState.results.map { result ->
                                if (result.userId == userId) {
                                    result.copy(relation = FriendRelation.REQUEST_SENT)
                                } else {
                                    result
                                }
                            }
                        )
                    }
                    _friendsChanged.tryEmit(Unit)
                }
                .onFailure { exception ->
                    _state.update {
                        it.copy(
                            sendingToUserId = null,
                            errorUserId = userId,
                            errorMessage = messageFor(exception)
                        )
                    }
                }
        }
    }

    private suspend fun runSearch() {
        _state.update { it.copy(isLoading = true, loadFailed = false) }

        searchUsers(_state.value.query)
            .onSuccess { results ->
                _state.update { it.copy(isLoading = false, results = results) }
            }
            .onFailure {
                _state.update { it.copy(isLoading = false, loadFailed = true) }
            }
    }
}
