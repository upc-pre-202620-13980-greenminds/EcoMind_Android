package pe.greenminds.ecomind.users.interfaces.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.users.application.GetCurrentProfileUseCase
import pe.greenminds.ecomind.users.application.GetFamilyUseCase
import pe.greenminds.ecomind.users.application.GetFriendsUseCase
import pe.greenminds.ecomind.monetization.interfaces.acl.MonetizationContextFacade
import pe.greenminds.ecomind.gamification.interfaces.acl.GamificationContextFacade
import pe.greenminds.ecomind.shared.application.GemBalanceStore
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getCurrentProfile: GetCurrentProfileUseCase,
    private val getFriends: GetFriendsUseCase,
    private val getFamily: GetFamilyUseCase,
    private val monetization: MonetizationContextFacade,
    private val gamification: GamificationContextFacade,
    private val gemBalanceStore: GemBalanceStore
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileUiState())
    val state: StateFlow<ProfileUiState> = _state.asStateFlow()
    val gemBalance: StateFlow<Int?> = gemBalanceStore.balance

    init {
        viewModelScope.launch {
            getCurrentProfile.observeSaved()?.collect { profile ->
                _state.update { it.copy(isLoadingProfile = false, profileFailed = false, profile = profile) }
            }
        }
    }

    fun onTabSelected(tab: ProfileTab) {
        _state.update { currentState ->
            currentState.copy(selectedTab = tab)
        }
    }

    // Also used by "Try again": everything is requested again from the start
    private var profileJob: Job? = null

    fun loadProfile(online: Boolean = true) {
        profileJob?.cancel()
        profileJob = viewModelScope.launch {
            _state.update { currentState ->
                currentState.copy(isLoadingProfile = currentState.profile == null, profileFailed = false)
            }

            getCurrentProfile()
                .onSuccess { profile ->
                    gemBalanceStore.initializeIfEmpty(profile.gemBalance)
                    _state.update { currentState ->
                        currentState.copy(isLoadingProfile = false, profile = profile)
                    }
                    // The other tabs need the id of the user, so they start after the profile
                    loadEquippedCosmetics()
                    if (online) {
                        loadFriends(profile.id)
                        loadFamily(profile.id)
                        loadAchievements()
                    }
                }
                .onFailure {
                    _state.update { currentState ->
                        currentState.copy(isLoadingProfile = false, profileFailed = currentState.profile == null)
                    }
                }
        }
    }

    fun loadEquippedCosmetics() {
        viewModelScope.launch {
            val equipped = monetization.getEquippedCosmetics()
            _state.update { it.copy(equippedCosmetics = equipped) }
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

    // The medals come from Gamification; Users only reads them through its facade
    private fun loadAchievements() {
        viewModelScope.launch {
            _state.update { currentState ->
                currentState.copy(isLoadingAchievements = true, achievementsFailed = false)
            }

            val result = try {
                gamification.getMyAchievements()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Result.failure(exception)
            }

            result
                .onSuccess { achievements ->
                    _state.update { currentState ->
                        currentState.copy(isLoadingAchievements = false, myAchievements = achievements)
                    }
                }
                .onFailure {
                    _state.update { currentState ->
                        currentState.copy(isLoadingAchievements = false, achievementsFailed = true)
                    }
                }
        }
    }
}
