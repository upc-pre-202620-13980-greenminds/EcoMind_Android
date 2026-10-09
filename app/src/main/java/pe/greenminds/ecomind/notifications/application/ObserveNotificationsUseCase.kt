package pe.greenminds.ecomind.notifications.application

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.notifications.domain.model.*
import pe.greenminds.ecomind.notifications.domain.repositories.NotificationRepository
import javax.inject.Inject

class ObserveNotificationsUseCase @Inject constructor(private val repository: NotificationRepository, private val sessions: SessionRepository) {
    val isSimulated get() = repository.isSimulated
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(requireSession: Boolean = true): Flow<List<AppNotification>> = sessions.getSession().flatMapLatest { session ->
        if (session == null || session.isExpired(System.currentTimeMillis())) if (requireSession) flow { throw NotificationSessionRequiredException() } else flowOf(emptyList())
        else repository.observe(session.accountId)
    }
}
