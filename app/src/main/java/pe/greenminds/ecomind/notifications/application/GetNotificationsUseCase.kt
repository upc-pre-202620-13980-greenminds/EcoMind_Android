package pe.greenminds.ecomind.notifications.application

import kotlinx.coroutines.flow.first
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.notifications.domain.model.Notification
import pe.greenminds.ecomind.notifications.domain.repositories.NotificationRepository
import pe.greenminds.ecomind.notifications.domain.repositories.PushNotifier
import pe.greenminds.ecomind.shared.domain.repositories.PreferencesRepository
import javax.inject.Inject

// The inbox only shows the categories enabled in the preferences of frame 70 (TS-008)
class GetNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val preferencesRepository: PreferencesRepository,
    private val sessionRepository: SessionRepository,
    private val pushNotifier: PushNotifier,
    private val unreadNotificationsStore: UnreadNotificationsStore
) {

    suspend operator fun invoke(): Result<List<Notification>> {
        val enabled = preferencesRepository.getEnabledCategories().first()

        return notificationRepository.getNotifications()
            .map { notifications -> notifications.filter { it.category in enabled } }
            .onSuccess { notifications ->
                // The list is newer than the last count, so the bell follows it
                unreadNotificationsStore.update(notifications.count { !it.isRead })

                // What the user sees here is not shown again as an alert of the phone
                sessionRepository.getSession().first()?.let { session ->
                    pushNotifier.markAsShown(session.accountId, notifications)
                }
            }
    }
}
