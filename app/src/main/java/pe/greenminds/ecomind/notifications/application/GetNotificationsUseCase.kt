package pe.greenminds.ecomind.notifications.application

import pe.greenminds.ecomind.notifications.domain.model.Notification
import pe.greenminds.ecomind.notifications.domain.repositories.NotificationRepository
import javax.inject.Inject

// The inbox shows every category: the preferences of frame 70 only apply to push (TS-008)
class GetNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val unreadNotificationsStore: UnreadNotificationsStore
) {

    suspend operator fun invoke(): Result<List<Notification>> {
        return notificationRepository.getNotifications()
            .onSuccess { notifications ->
                // The list is newer than the last count, so the bell follows it
                unreadNotificationsStore.update(notifications.count { !it.isRead })
            }
    }
}
