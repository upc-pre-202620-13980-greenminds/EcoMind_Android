package pe.greenminds.ecomind.notifications.interfaces.inbox

import pe.greenminds.ecomind.notifications.domain.model.AppNotification

data class NotificationsUiState(val isLoading: Boolean = true, val items: List<AppNotification> = emptyList(), val hasError: Boolean = false, val sessionRequired: Boolean = false, val isSaving: Boolean = false, val isSimulated: Boolean = true) {
    val unreadCount get() = items.count { !it.isRead }
}
