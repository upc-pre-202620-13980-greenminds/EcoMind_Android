package pe.greenminds.ecomind.notifications.infrastructure.remote

import pe.greenminds.ecomind.shared.domain.model.NotificationCategory
import pe.greenminds.ecomind.notifications.domain.model.Notification
import java.time.Instant

fun NotificationDto.toDomain(): Notification {
    return Notification(
        id = id,
        // A category the app does not know yet is shown as community activity
        category = NotificationCategory.entries.find { it.name == category }
            ?: NotificationCategory.COMMUNITY_ACTIVITY,
        title = title,
        message = message,
        isRead = read,
        createdAtMillis = Instant.parse(createdAt).toEpochMilli()
    )
}
