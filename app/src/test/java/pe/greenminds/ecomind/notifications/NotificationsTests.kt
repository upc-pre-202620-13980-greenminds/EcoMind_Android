package pe.greenminds.ecomind.notifications

import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import pe.greenminds.ecomind.iam.domain.model.Session
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.notifications.application.*
import pe.greenminds.ecomind.notifications.domain.model.*
import pe.greenminds.ecomind.notifications.domain.repositories.NotificationRepository
import pe.greenminds.ecomind.settings.domain.model.NotificationCategory

class NotificationsTests {
    @Test fun readsUseAuthenticatedAccountForSingleAndAllNotifications() = runBlocking {
        val repository = FakeNotifications()
        val read = ReadNotificationsUseCase(repository, FakeSession(session(42)))
        read("medal").getOrThrow()
        assertEquals(42L to "medal", repository.lastRead)
        read().getOrThrow()
        assertEquals(42L to null, repository.lastRead)
    }
    @Test fun missingAndExpiredSessionsCannotMutateNotifications() = runBlocking {
        for (current in listOf(null, session(1).copy(expiresAtMillis = 0))) {
            val repository = FakeNotifications()
            assertTrue(ReadNotificationsUseCase(repository, FakeSession(current))().exceptionOrNull() is NotificationSessionRequiredException)
            assertNull(repository.lastRead)
        }
    }
    @Test fun signedOutHeaderHasNoUnreadNotifications() = runBlocking {
        val repository = FakeNotifications()
        assertTrue(ObserveNotificationsUseCase(repository, FakeSession(null))(requireSession = false).first().isEmpty())
        assertNull(repository.lastObserved)
    }
    @Test fun inboxUsesCurrentAccountAndPreservesStorageFailure() = runBlocking {
        val repository = FakeNotifications()
        val sessions = FakeSession(session(27))
        assertEquals("medal", ObserveNotificationsUseCase(repository, sessions)().first().single().id)
        assertEquals(27L, repository.lastObserved)
        val error = IllegalStateException("Storage unavailable")
        repository.failure = error
        assertSame(error, ReadNotificationsUseCase(repository, sessions)("medal").exceptionOrNull())
    }
    private class FakeNotifications : NotificationRepository {
        override val isSimulated = true
        var lastRead: Pair<Long, String?>? = null
        var lastObserved: Long? = null
        var failure: Throwable? = null
        override fun observe(accountId: Long): Flow<List<AppNotification>> { lastObserved = accountId; return flowOf(listOf(AppNotification("medal", NotificationCategory.ACHIEVEMENTS, 1))) }
        override suspend fun markRead(accountId: Long, id: String): Result<Unit> { lastRead = accountId to id; return failure?.let { Result.failure(it) } ?: Result.success(Unit) }
        override suspend fun markAllRead(accountId: Long): Result<Unit> { lastRead = accountId to null; return Result.success(Unit) }
    }
    private class FakeSession(value: Session?) : SessionRepository {
        val current = MutableStateFlow(value)
        override fun getSession() = current
        override suspend fun saveSession(session: Session) { current.value = session }
        override suspend fun clearSession() { current.value = null }
    }
    private fun session(id: Long) = Session(id, "demo@example.com", "demo", Long.MAX_VALUE)
}
