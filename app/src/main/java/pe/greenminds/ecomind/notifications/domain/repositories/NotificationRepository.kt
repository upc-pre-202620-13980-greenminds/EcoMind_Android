package pe.greenminds.ecomind.notifications.domain.repositories

import pe.greenminds.ecomind.notifications.domain.model.Notification

// The user always comes from the token, so no method needs an id of a user
interface NotificationRepository {

    // The newest first
    suspend fun getNotifications(): Result<List<Notification>>

    suspend fun markAsRead(notificationId: Long): Result<Notification>

    suspend fun markAllAsRead(): Result<Unit>

    suspend fun getUnreadCount(): Result<Int>
}
