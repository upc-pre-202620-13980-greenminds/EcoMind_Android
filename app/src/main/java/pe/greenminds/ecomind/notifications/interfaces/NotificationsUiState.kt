package pe.greenminds.ecomind.notifications.interfaces

import androidx.annotation.StringRes
import pe.greenminds.ecomind.notifications.domain.model.Notification

data class NotificationsUiState(
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val notifications: List<Notification> = emptyList(),
    val isMarkingAll: Boolean = false,
    // Shown when a notification could not be marked as read; the list keeps its state
    @StringRes val actionError: Int? = null
) {
    val unreadCount: Int get() = notifications.count { !it.isRead }
}
