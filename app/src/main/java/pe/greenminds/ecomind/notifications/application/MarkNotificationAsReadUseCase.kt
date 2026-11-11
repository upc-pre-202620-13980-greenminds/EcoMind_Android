package pe.greenminds.ecomind.notifications.application

import pe.greenminds.ecomind.notifications.domain.model.Notification
import pe.greenminds.ecomind.notifications.domain.repositories.NotificationRepository
import javax.inject.Inject

class MarkNotificationAsReadUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val refreshUnreadCount: RefreshUnreadCountUseCase
) {

    suspend operator fun invoke(notificationId: Long): Result<Notification> {
        return notificationRepository.markAsRead(notificationId)
            .onSuccess { refreshUnreadCount() }
    }
}
