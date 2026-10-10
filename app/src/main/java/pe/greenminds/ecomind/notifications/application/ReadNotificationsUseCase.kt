package pe.greenminds.ecomind.notifications.application

import kotlinx.coroutines.flow.first
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.notifications.domain.model.NotificationSessionRequiredException
import pe.greenminds.ecomind.notifications.domain.repositories.NotificationRepository
import javax.inject.Inject

class ReadNotificationsUseCase @Inject constructor(private val repository: NotificationRepository, private val sessions: SessionRepository) {
    suspend operator fun invoke(id: String? = null): Result<Unit> {
        val session = sessions.getSession().first()
        if (session == null || session.isExpired(System.currentTimeMillis())) return Result.failure(NotificationSessionRequiredException())
        return if (id == null) repository.markAllRead(session.accountId) else repository.markRead(session.accountId, id)
    }
}
