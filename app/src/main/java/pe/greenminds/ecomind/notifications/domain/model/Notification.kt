package pe.greenminds.ecomind.notifications.domain.model

import pe.greenminds.ecomind.shared.domain.model.NotificationCategory

// A notification of the inbox (TS-016). The web services already send the texts in the
// language of the request, so the app shows them as they come.
data class Notification(
    val id: Long,
    val category: NotificationCategory,
    val title: String,
    val message: String,
    val isRead: Boolean,
    // Milliseconds; used to show "5 min", "1 h" or "Yesterday"
    val createdAtMillis: Long,
    // Last, with a default, so the previews that only need the texts stay short
    val type: NotificationType = NotificationType.UNKNOWN
)
