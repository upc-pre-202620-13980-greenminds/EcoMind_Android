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

@HiltViewModel
class MainShellViewModel @Inject constructor(
    private val getCurrentProfile: GetCurrentProfileUseCase,
    private val gemBalanceStore: GemBalanceStore,
    private val refreshUnreadCount: RefreshUnreadCountUseCase,
    private val unreadNotificationsStore: UnreadNotificationsStore
) : ViewModel() {

    private val _state = MutableStateFlow(MainShellUiState())
    val state: StateFlow<MainShellUiState> = _state.asStateFlow()
    private var balanceRequest: Job? = null
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

    // Called when the bars appear, because before signing in there is no profile to read
    fun loadBalances() {
        balanceRequest?.cancel()
        balanceRequest = viewModelScope.launch {
            getCurrentProfile().onSuccess { profile ->
                gemBalanceStore.update(profile.gemBalance)
                _state.update { currentState ->
                    currentState.copy(
                        gemBalance = profile.gemBalance,
                        ecopoints = profile.ecopoints
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

    fun clearBalances() {
        balanceRequest?.cancel()
        unreadRequest?.cancel()
        gemBalanceStore.clear()
        unreadNotificationsStore.clear()
        _state.value = MainShellUiState()
    }
}
