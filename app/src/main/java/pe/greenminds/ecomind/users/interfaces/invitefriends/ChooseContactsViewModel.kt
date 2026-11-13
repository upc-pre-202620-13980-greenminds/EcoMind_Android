package pe.greenminds.ecomind.users.interfaces.invitefriends

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
import pe.greenminds.ecomind.users.application.SendInvitationsUseCase
import pe.greenminds.ecomind.users.domain.model.FriendRelation
import pe.greenminds.ecomind.users.domain.model.UserSearchResult
import pe.greenminds.ecomind.users.interfaces.friendrequests.messageFor
import javax.inject.Inject

@HiltViewModel
class ChooseContactsViewModel @Inject constructor(
    private val searchUsers: SearchUsersUseCase,
    private val sendInvitations: SendInvitationsUseCase
) : ViewModel() {

    companion object {
        // Waits for the user to stop typing before asking the web services again
        private const val SEARCH_DELAY_MILLIS = 300L
    }

    private val _state = MutableStateFlow(ChooseContactsUiState())
    val state: StateFlow<ChooseContactsUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<ChooseContactsEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<ChooseContactsEvent> = _events.asSharedFlow()

    // Tells the screen that requests were sent, so Profile can load its friends again
    private val _friendsChanged = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val friendsChanged: SharedFlow<Unit> = _friendsChanged.asSharedFlow()

    private var searchJob: Job? = null

    init {
        search()
    }

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DELAY_MILLIS)
            runSearch()
        }
    }

    // Also used by "Try again"
    fun search() {
        searchJob?.cancel()
        searchJob = viewModelScope.launch { runSearch() }
    }

    fun onToggle(contact: UserSearchResult) {
        _state.update { currentState ->
            val isSelected = currentState.selected.any { it.userId == contact.userId }
            if (isSelected) {
                currentState.copy(
                    selected = currentState.selected.filter { it.userId != contact.userId },
                    contacts = listOf(contact) + currentState.contacts,
                    errorMessage = null
                )
            } else {
                currentState.copy(
                    selected = currentState.selected + contact,
                    contacts = currentState.contacts.filter { it.userId != contact.userId },
                    errorMessage = null
                )
            }
        }
    }

    fun sendInvitations() {
        val current = _state.value
        if (current.isSending || current.selected.isEmpty()) return

        viewModelScope.launch {
            _state.update { it.copy(isSending = true, errorMessage = null) }

            val result = sendInvitations(current.selected.map { it.userId })
            if (result.sent.isNotEmpty()) _friendsChanged.tryEmit(Unit)

            if (result.failedUserIds.isEmpty()) {
                _state.update { it.copy(isSending = false) }
                val event = if (result.sent.size == 1) {
                    ChooseContactsEvent.OpenStatus(result.sent.first().id)
                } else {
                    ChooseContactsEvent.OpenRequests
                }
                _events.tryEmit(event)
            } else {
                // Only the contacts that failed stay selected, so "Send invitation" retries them
                _state.update { currentState ->
                    currentState.copy(
                        isSending = false,
                        selected = currentState.selected.filter { it.userId in result.failedUserIds },
                        errorMessage = result.firstError?.let { messageFor(it) }
                    )
                }
            }
        }
    }

    private suspend fun runSearch() {
        _state.update { it.copy(isLoading = true, loadFailed = false) }

        searchUsers(_state.value.query)
            .onSuccess { results ->
                _state.update { currentState ->
                    val selectedIds = currentState.selected.map { it.userId }.toSet()
                    // Friends and users with a request already sent cannot be invited again
                    val contacts = results.filter {
                        it.relation == FriendRelation.NONE && it.userId !in selectedIds
                    }
                    currentState.copy(isLoading = false, contacts = contacts)
                }
            }
            .onFailure {
                _state.update { it.copy(isLoading = false, loadFailed = true) }
            }
    }
}
