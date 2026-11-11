package pe.greenminds.ecomind.notifications.application

import pe.greenminds.ecomind.notifications.domain.repositories.NotificationRepository
import javax.inject.Inject

// Asks the web services for the unread count and keeps it in the store of the bell
class RefreshUnreadCountUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val unreadNotificationsStore: UnreadNotificationsStore
) {

    suspend operator fun invoke(): Result<Int> {
        return notificationRepository.getUnreadCount()
            .onSuccess { count -> unreadNotificationsStore.update(count) }
    }
}
