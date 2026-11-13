package pe.greenminds.ecomind.iam.application

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.greenminds.ecomind.iam.domain.model.Session
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import java.io.IOException

class ClearSessionUseCaseTest {

    @Test
    fun reportsSuccessfulSessionCleanup() = runBlocking {
        var cleared = false
        val useCase = ClearSessionUseCase(FakeSessionRepository {
            cleared = true
        })

        assertTrue(useCase())
        assertTrue(cleared)
    }

    @Test
    fun reportsStorageFailureWithoutThrowing() = runBlocking {
        val useCase = ClearSessionUseCase(FakeSessionRepository {
            throw IOException("Session storage unavailable")
        })

        assertFalse(useCase())
    }

    @Test
    fun completesSessionCleanupWhenCallerIsCancelled() = runBlocking {
        val started = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        var cleared = false
        val useCase = ClearSessionUseCase(FakeSessionRepository {
            started.complete(Unit)
            release.await()
            cleared = true
        })
        val job = launch { useCase() }

        withTimeout(5_000) { started.await() }
        job.cancel()
        release.complete(Unit)
        job.cancelAndJoin()

        assertTrue(job.isCancelled)
        assertTrue(cleared)
    }

    private class FakeSessionRepository(
        private val clear: suspend () -> Unit
    ) : SessionRepository {
        override fun getSession(): Flow<Session?> = emptyFlow()

        override suspend fun saveSession(session: Session) = Unit

        override suspend fun clearSession() = clear()
    }
}
