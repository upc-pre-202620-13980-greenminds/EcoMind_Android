package pe.greenminds.ecomind.notifications.domain.repositories

import kotlinx.coroutines.flow.Flow
import pe.greenminds.ecomind.notifications.domain.model.AppNotification

interface NotificationRepository {
    val isSimulated: Boolean
    fun observe(accountId: Long): Flow<List<AppNotification>>
    suspend fun markRead(accountId: Long, id: String): Result<Unit>
    suspend fun markAllRead(accountId: Long): Result<Unit>
}
