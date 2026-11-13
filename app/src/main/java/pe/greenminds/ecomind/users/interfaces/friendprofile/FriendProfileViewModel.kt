package pe.greenminds.ecomind.users.interfaces.friendprofile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.users.application.GetFriendProfileUseCase
import pe.greenminds.ecomind.users.application.RemoveFriendUseCase
import pe.greenminds.ecomind.users.domain.model.FriendRequestError
import pe.greenminds.ecomind.users.domain.model.FriendRequestException
import pe.greenminds.ecomind.users.interfaces.friendrequests.messageFor
import javax.inject.Inject

@HiltViewModel
class FriendProfileViewModel @Inject constructor(
    private val getFriendProfile: GetFriendProfileUseCase,
    private val removeFriend: RemoveFriendUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(FriendProfileUiState())
    val state: StateFlow<FriendProfileUiState> = _state.asStateFlow()

    // The id comes from the route; the screen calls this when it is shown and on "Try again"
    fun loadProfile(friendId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, loadFailed = false) }

            getFriendProfile(friendId)
                .onSuccess { profile ->
                    _state.update { it.copy(isLoading = false, profile = profile) }
                }
                .onFailure {
                    _state.update { it.copy(isLoading = false, loadFailed = true) }
                }
        }
    }

    fun onRemoveRequested() {
        _state.update { it.copy(isConfirmingRemoval = true, removeError = null) }
    }

    fun onRemoveCancelled() {
        _state.update { it.copy(isConfirmingRemoval = false, removeError = null) }
    }

    fun confirmRemoval() {
        val current = _state.value
        val friendshipId = current.profile?.friendshipId ?: return
        if (current.isRemoving) return

        viewModelScope.launch {
            _state.update { it.copy(isRemoving = true, removeError = null) }

            removeFriend(friendshipId)
                .onSuccess {
                    _state.update { it.copy(isRemoving = false, isRemoved = true) }
                }
                .onFailure { exception ->
                    // The card stays open so the user can try again
                    _state.update { it.copy(isRemoving = false, removeError = errorFor(exception)) }
                }
        }
    }

    // The 403 and 422 of the web services have their own text; anything else is a generic failure
    private fun errorFor(exception: Throwable): Int {
        val error = (exception as? FriendRequestException)?.error
        return if (
            error == FriendRequestError.NOT_PART_OF_FRIENDSHIP ||
            error == FriendRequestError.IN_WAITING_PERIOD
        ) {
            messageFor(exception)
        } else {
            R.string.friend_profile_remove_failed
        }
    }
}
