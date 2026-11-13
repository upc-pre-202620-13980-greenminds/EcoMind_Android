package pe.greenminds.ecomind.notifications.infrastructure.remote

import pe.greenminds.ecomind.shared.domain.model.NotificationCategory
import pe.greenminds.ecomind.notifications.domain.model.Notification
import pe.greenminds.ecomind.notifications.domain.model.NotificationType
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
        createdAtMillis = Instant.parse(createdAt).toEpochMilli(),
        // A type of a newer version of the web services only opens nothing
        type = NotificationType.entries.find { it.name == type } ?: NotificationType.UNKNOWN
    )
}

// One notification that cannot be read, such as one with a wrong date, is left out,
// so the rest of the inbox is still shown
fun List<NotificationDto>.toDomainSkippingInvalid(): List<Notification> {
    return mapNotNull { dto -> runCatching { dto.toDomain() }.getOrNull() }
}
