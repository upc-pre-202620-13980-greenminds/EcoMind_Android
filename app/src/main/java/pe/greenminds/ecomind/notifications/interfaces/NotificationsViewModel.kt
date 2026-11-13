package pe.greenminds.ecomind.notifications.interfaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.notifications.application.GetNotificationsUseCase
import pe.greenminds.ecomind.notifications.application.MarkAllNotificationsAsReadUseCase
import pe.greenminds.ecomind.notifications.application.MarkNotificationAsReadUseCase
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val getNotifications: GetNotificationsUseCase,
    private val markNotificationAsRead: MarkNotificationAsReadUseCase,
    private val markAllNotificationsAsRead: MarkAllNotificationsAsReadUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsUiState())
    val state: StateFlow<NotificationsUiState> = _state.asStateFlow()

    init {
        loadNotifications()
    }

    // Also used by "Try again" (frame 67)
    fun loadNotifications() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, loadFailed = false, actionError = null) }

            getNotifications()
                .onSuccess { notifications ->
                    _state.update { it.copy(isLoading = false, notifications = notifications) }
                }
                .onFailure {
                    _state.update { it.copy(isLoading = false, loadFailed = true) }
                }
        }
    }

    // Opening a notification marks it as read and, if it has one, opens its screen.
    // A read one does not call the web services again.
    fun onNotificationClick(notificationId: Long) {
        val notification = _state.value.notifications.find { it.id == notificationId } ?: return
        destinationFor(notification.type)?.let { destination ->
            _state.update { it.copy(openDestination = destination) }
        }
        if (notification.isRead) return

        viewModelScope.launch {
            _state.update { it.copy(actionError = null) }

            markNotificationAsRead(notificationId)
                .onSuccess { updated ->
                    _state.update { currentState ->
                        currentState.copy(
                            notifications = currentState.notifications.map {
                                if (it.id == updated.id) updated else it
                            }
                        )
                    }
                }
                .onFailure {
                    _state.update { it.copy(actionError = R.string.notifications_action_failed) }
                }
        }
    }

    fun onDestinationOpened() {
        _state.update { it.copy(openDestination = null) }
    }

    fun markAllAsRead() {
        val current = _state.value
        if (current.isMarkingAll || current.unreadCount == 0) return

        viewModelScope.launch {
            _state.update { it.copy(isMarkingAll = true, actionError = null) }

            markAllNotificationsAsRead()
                .onSuccess {
                    _state.update { currentState ->
                        currentState.copy(
                            isMarkingAll = false,
                            notifications = currentState.notifications.map {
                                it.copy(isRead = true)
                            }
                        )
                    }
                }
                .onFailure {
                    _state.update {
                        it.copy(
                            isMarkingAll = false,
                            actionError = R.string.notifications_action_failed
                        )
                    }
                }
        }
    }
}
