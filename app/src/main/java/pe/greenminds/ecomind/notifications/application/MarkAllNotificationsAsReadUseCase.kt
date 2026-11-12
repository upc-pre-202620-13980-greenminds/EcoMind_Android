package pe.greenminds.ecomind.notifications.application

import pe.greenminds.ecomind.notifications.domain.repositories.NotificationRepository
import javax.inject.Inject

class MarkAllNotificationsAsReadUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val unreadNotificationsStore: UnreadNotificationsStore
) {

    suspend operator fun invoke(): Result<Unit> {
        return notificationRepository.markAllAsRead()
            .onSuccess { unreadNotificationsStore.clear() }
    }
}
