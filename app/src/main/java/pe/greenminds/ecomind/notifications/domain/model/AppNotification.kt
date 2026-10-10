package pe.greenminds.ecomind.notifications.domain.model

import pe.greenminds.ecomind.settings.domain.model.NotificationCategory

data class AppNotification(val id: String, val category: NotificationCategory, val ageMinutes: Int, val isRead: Boolean = false)
class NotificationSessionRequiredException : IllegalStateException("Session required")
