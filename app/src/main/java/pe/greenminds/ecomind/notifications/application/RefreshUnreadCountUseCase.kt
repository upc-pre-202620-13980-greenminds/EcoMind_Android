package pe.greenminds.ecomind.notifications.application

import kotlinx.coroutines.flow.first
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.notifications.domain.repositories.NotificationRepository
import pe.greenminds.ecomind.notifications.domain.repositories.PushNotifier
import pe.greenminds.ecomind.shared.domain.repositories.PreferencesRepository
import javax.inject.Inject

// Runs each time the top bar is shown. The bell counts the same list as the inbox, so it reads
// the notifications instead of the unread count of the web services, which counts every category.
// The new ones of an enabled category are also shown as alerts of the phone.
class RefreshUnreadCountUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val preferencesRepository: PreferencesRepository,
    private val sessionRepository: SessionRepository,
    private val pushNotifier: PushNotifier,
    private val unreadNotificationsStore: UnreadNotificationsStore
) {

    suspend operator fun invoke(): Result<Int> {
        val enabled = preferencesRepository.getEnabledCategories().first()

        return notificationRepository.getNotifications()
            .map { notifications -> notifications.filter { it.category in enabled } }
            .onSuccess { notifications ->
                sessionRepository.getSession().first()?.let { session ->
                    pushNotifier.showNew(session.accountId, notifications)
                }
            }
            .map { notifications -> notifications.count { !it.isRead } }
            .onSuccess { count -> unreadNotificationsStore.update(count) }
    }
}
