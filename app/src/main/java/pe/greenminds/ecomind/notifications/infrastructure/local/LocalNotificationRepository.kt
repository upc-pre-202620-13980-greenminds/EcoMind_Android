package pe.greenminds.ecomind.notifications.infrastructure.local

import androidx.datastore.preferences.core.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import pe.greenminds.ecomind.notifications.domain.model.AppNotification
import pe.greenminds.ecomind.notifications.domain.repositories.NotificationRepository
import pe.greenminds.ecomind.settings.domain.model.NotificationCategory
import pe.greenminds.ecomind.shared.infrastructure.ExperienceDataStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalNotificationRepository @Inject constructor(private val store: ExperienceDataStore) : NotificationRepository {
    override val isSimulated = true
    private val examples = listOf(
        AppNotification("learning", NotificationCategory.LEARNING, 5),
        AppNotification("quest", NotificationCategory.QUESTS, 60),
        AppNotification("medal", NotificationCategory.ACHIEVEMENTS, 180),
        AppNotification("reaction", NotificationCategory.COMMUNITY, 1440, true),
        AppNotification("shared", NotificationCategory.COMMUNITY, 1440, true),
        AppNotification("event", NotificationCategory.EVENTS, 2880, true),
        AppNotification("cancelled", NotificationCategory.EVENTS, 2880, true)
    )
    private fun key(accountId: Long) = stringSetPreferencesKey("read_notifications_$accountId")
    override fun observe(accountId: Long) = store.dataStore.data.map { values ->
        if (accountId != 1L) emptyList() else examples.map { it.copy(isRead = it.isRead || it.id in values[key(accountId)].orEmpty()) }
    }
    override suspend fun markRead(accountId: Long, id: String): Result<Unit> {
        if (accountId != 1L || examples.none { it.id == id }) return Result.failure(NoSuchElementException("Unknown notification"))
        return write(accountId, setOf(id))
    }
    override suspend fun markAllRead(accountId: Long) = write(accountId, if (accountId == 1L) examples.map { it.id }.toSet() else emptySet())
    private suspend fun write(accountId: Long, ids: Set<String>): Result<Unit> = try {
        store.dataStore.edit { it[key(accountId)] = it[key(accountId)].orEmpty() + ids }
        Result.success(Unit)
    } catch (cancelled: CancellationException) { throw cancelled }
      catch (exception: Exception) { Result.failure(exception) }
}
