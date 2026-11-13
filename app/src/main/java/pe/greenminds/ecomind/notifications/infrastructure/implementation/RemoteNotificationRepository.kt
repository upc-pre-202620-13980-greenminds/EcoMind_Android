package pe.greenminds.ecomind.notifications.infrastructure.implementation

import pe.greenminds.ecomind.shared.infrastructure.remote.apiResult
import pe.greenminds.ecomind.shared.infrastructure.remote.requireBody
import pe.greenminds.ecomind.notifications.domain.model.Notification
import pe.greenminds.ecomind.notifications.domain.repositories.NotificationRepository
import pe.greenminds.ecomind.notifications.infrastructure.remote.NotificationsApi
import pe.greenminds.ecomind.notifications.infrastructure.remote.toDomain
import retrofit2.HttpException
import javax.inject.Inject

class RemoteNotificationRepository @Inject constructor(
    private val api: NotificationsApi
) : NotificationRepository {

    override suspend fun getNotifications(): Result<List<Notification>> = apiResult {
        api.getNotifications().requireBody().map { it.toDomain() }
    }

    override suspend fun markAsRead(notificationId: Long): Result<Notification> = apiResult {
        api.markAsRead(notificationId).requireBody().toDomain()
    }

    override suspend fun markAllAsRead(): Result<Unit> = apiResult {
        // The answer has no body, so only the status code is checked
        val response = api.markAllAsRead()
        if (!response.isSuccessful) throw HttpException(response)
    }

    override suspend fun getUnreadCount(): Result<Int> = apiResult {
        api.getUnreadCount().requireBody().count.toInt()
    }
}
