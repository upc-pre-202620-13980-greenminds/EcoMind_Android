package pe.greenminds.ecomind.users.interfaces.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.users.application.GetCurrentProfileUseCase
import pe.greenminds.ecomind.users.application.GetFamilyUseCase
import pe.greenminds.ecomind.users.application.GetFriendsUseCase
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentProfile: GetCurrentProfileUseCase,
    private val getFriends: GetFriendsUseCase,
    private val getFamily: GetFamilyUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    fun onTabSelected(tab: ProfileTab) {
        _state.update { currentState ->
            currentState.copy(selectedTab = tab)
        }
    }

    // Also used by "Try again": everything is requested again from the start
    fun loadProfile() {
        viewModelScope.launch {
            _state.update { currentState ->
                currentState.copy(isLoadingProfile = true, profileFailed = false)
            }

            getCurrentProfile()
                .onSuccess { profile ->
                    _state.update { currentState ->
                        currentState.copy(isLoadingProfile = false, profile = profile)
                    }
                    // The other tabs need the id of the user, so they start after the profile
                    loadFriends(profile.id)
                    loadFamily(profile.id)
                }
                .onFailure {
                    _state.update { currentState ->
                        currentState.copy(isLoadingProfile = false, profileFailed = true)
                    }
                }
        }
    }

    fun retryFriends() {
        _state.value.profile?.let { profile -> loadFriends(profile.id) }
    }

    fun retryFamily() {
        _state.value.profile?.let { profile -> loadFamily(profile.id) }
    }

    // Each tab loads in its own coroutine, so a slow one does not block the other
    private fun loadFriends(userId: Long) {
        viewModelScope.launch {
            _state.update { currentState ->
                currentState.copy(isLoadingFriends = true, friendsFailed = false)
            }

            getFriends(userId)
                .onSuccess { friends ->
                    _state.update { currentState ->
                        currentState.copy(isLoadingFriends = false, friends = friends)
                    }
                }
                .onFailure {
                    _state.update { currentState ->
                        currentState.copy(isLoadingFriends = false, friendsFailed = true)
                    }
                }
        }
    }

    private fun loadFamily(userId: Long) {
        viewModelScope.launch {
            _state.update { currentState ->
                currentState.copy(isLoadingFamily = true, familyFailed = false)
            }

            getFamily(userId)
                .onSuccess { family ->
                    _state.update { currentState ->
                        currentState.copy(isLoadingFamily = false, family = family)
                    }
                }
                .onFailure {
                    _state.update { currentState ->
                        currentState.copy(isLoadingFamily = false, familyFailed = true)
                    }
                }
        }
    }
}
