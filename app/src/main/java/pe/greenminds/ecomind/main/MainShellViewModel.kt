package pe.greenminds.ecomind.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.MutableStateFlow
import pe.greenminds.ecomind.notifications.application.ObserveNotificationsUseCase
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.users.application.GetCurrentProfileUseCase
import pe.greenminds.ecomind.shared.application.GemBalanceStore
import javax.inject.Inject

@HiltViewModel
class MainShellViewModel @Inject constructor(
    private val getCurrentProfile: GetCurrentProfileUseCase,
    observeNotifications: ObserveNotificationsUseCase,
    private val gemBalanceStore: GemBalanceStore
) : ViewModel() {

    private val _state = MutableStateFlow(MainShellUiState())
    val state: StateFlow<MainShellUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            gemBalanceStore.balance.collect { balance ->
                balance?.let { _state.update { state -> state.copy(gemBalance = it) } }
            }
        }
        viewModelScope.launch {
            observeNotifications(requireSession = false).catch { emit(emptyList()) }.collect { items ->
                _state.update { it.copy(unreadCount = items.count { item -> !item.isRead }) }
            }
        }
    }

    // Called when the bars appear, because before signing in there is no profile to read
    fun loadBalances() {
        viewModelScope.launch {
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
}
