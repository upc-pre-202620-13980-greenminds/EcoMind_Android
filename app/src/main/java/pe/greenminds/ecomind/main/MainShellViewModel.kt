package pe.greenminds.ecomind.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import pe.greenminds.ecomind.users.application.GetCurrentProfileUseCase
import pe.greenminds.ecomind.shared.application.GemBalanceStore
import pe.greenminds.ecomind.notifications.application.RefreshUnreadCountUseCase
import pe.greenminds.ecomind.notifications.application.UnreadNotificationsStore
import javax.inject.Inject
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MainShellViewModel @Inject constructor(
    private val getCurrentProfile: GetCurrentProfileUseCase,
    private val gemBalanceStore: GemBalanceStore,
    private val refreshUnreadCount: RefreshUnreadCountUseCase,
    private val unreadNotificationsStore: UnreadNotificationsStore,
    sessions: SessionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(MainShellUiState())
    val state: StateFlow<MainShellUiState> = _state.asStateFlow()
    val session = sessions.getSession().stateIn(viewModelScope, SharingStarted.Eagerly, null)
    private var profileRequest: Job? = null
    private var unreadRequest: Job? = null

    init {
        // The inbox changes the count too, so the bell always follows the store
        viewModelScope.launch {
            unreadNotificationsStore.count.collect { count ->
                _state.update { currentState -> currentState.copy(unreadNotifications = count) }
            }
        }
        viewModelScope.launch {
            gemBalanceStore.balance.collect { balance ->
                balance?.let {
                    _state.update { currentState -> currentState.copy(gemBalance = it) }
                }
            }
        }
    }

    fun loadHeaderCounters() {
        profileRequest?.cancel()
        profileRequest = viewModelScope.launch {
            getCurrentProfile().onSuccess { profile ->
                gemBalanceStore.update(profile.gemBalance)
                _state.update { currentState ->
                    currentState.copy(
                        gemBalance = profile.gemBalance,
                        streak = profile.streak
                    )
                }
            }
        }
    }

    // Called each time another screen with the top bar is shown; a failure keeps the last count
    fun loadUnreadNotifications() {
        unreadRequest?.cancel()
        unreadRequest = viewModelScope.launch { refreshUnreadCount() }
    }

    fun clearHeader() {
        profileRequest?.cancel()
        unreadRequest?.cancel()
        gemBalanceStore.clear()
        unreadNotificationsStore.clear()
        _state.value = MainShellUiState()
    }
}
