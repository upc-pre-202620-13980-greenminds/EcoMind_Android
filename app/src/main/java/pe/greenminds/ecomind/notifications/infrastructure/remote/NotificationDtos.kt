package pe.greenminds.ecomind.notifications.infrastructure.remote

// Same fields, names and types as the resources of the web services (Notifications context)

// GET /api/v1/notification and PATCH /api/v1/notification/{id}/read
data class NotificationDto(
    val id: Long,
    val category: String,
    // GENERAL or FRIEND_REQUEST_ACCEPTED; the app does not need it to show the notification
    val type: String,
    val title: String,
    val message: String,
    val read: Boolean,
    // ISO-8601 date
    val createdAt: String
)

// GET /api/v1/notification/unread-count
data class UnreadCountDto(
    val count: Long
)
