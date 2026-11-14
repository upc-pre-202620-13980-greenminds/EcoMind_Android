package pe.greenminds.ecomind.gamification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import pe.greenminds.ecomind.gamification.application.GetAchievementsUseCase
import pe.greenminds.ecomind.gamification.application.GetAchievementByIdUseCase
import pe.greenminds.ecomind.gamification.domain.model.*
import pe.greenminds.ecomind.gamification.domain.repositories.AchievementRepository
import pe.greenminds.ecomind.iam.domain.model.Session
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository

// HU-011 E2: display the current student's collection without granting new medals.
class AchievementsTests {
    @Test
    fun collectionSeparatesEarnedAndAvailableAndDeduplicatesRepeatedAwards() = runBlocking {
        val repository = FakeAchievements(listOf(definition("earned"), definition("pending")),
            listOf(award("earned"), award("earned")))
        val collection = GetAchievementsUseCase(repository, FakeSessions())().getOrThrow()
        assertEquals(listOf("earned"), collection.earned.map { it.achievement.id })
        assertEquals(listOf("pending"), collection.available.map { it.achievement.id })
        assertTrue(collection.isSimulated)
    }

    @Test
    fun otherPeopleAndFamilyAwardsAreNeverShownAsPersonalMedals() = runBlocking {
        val repository = FakeAchievements(listOf(definition("a")),
            listOf(award("a", userId = 2), award("a", scope = "FAMILY")))
        val collection = GetAchievementsUseCase(repository, FakeSessions())().getOrThrow()
        assertTrue(collection.earned.isEmpty())
    }

    @Test
    fun archivedEarnedDefinitionsRemainInHistoryButUnavailableOnesAreHidden() = runBlocking {
        val repository = FakeAchievements(listOf(definition("old", active = false), definition("hidden", active = false)),
            listOf(award("old")))
        val collection = GetAchievementsUseCase(repository, FakeSessions())().getOrThrow()
        assertEquals(listOf("old"), collection.earned.map { it.achievement.id })
        assertTrue(collection.available.isEmpty())
    }

    @Test
    fun anEmptyCatalogAndNoAwardsProducesAnEmptyCollection() = runBlocking {
        val collection = GetAchievementsUseCase(FakeAchievements(emptyList(), emptyList()), FakeSessions())().getOrThrow()
        assertTrue(collection.entries.isEmpty())
    }

    @Test
    fun missingOrExpiredSessionsFailBeforeReadingData() = runBlocking {
        for (session in listOf(null, validSession().copy(expiresAtMillis = 0))) {
            val repository = FakeAchievements(emptyList(), emptyList())
            val result = GetAchievementsUseCase(repository, FakeSessions(session))()
            assertTrue(result.exceptionOrNull() is AchievementSessionRequiredException)
            assertEquals(0, repository.reads)
        }
    }

    @Test
    fun anAwardReadFailureIsNotPresentedAsAnEmptyCollection() = runBlocking {
        val repository = FakeAchievements(listOf(definition("a")), emptyList())
        repository.awardsFailure = IllegalStateException("Service unavailable")
        val result = GetAchievementsUseCase(repository, FakeSessions())()
        assertSame(repository.awardsFailure, result.exceptionOrNull())
    }

    @Test
    fun missingDefinitionsFailInsteadOfSilentlyHidingEarnedAwards() = runBlocking {
        val result = GetAchievementsUseCase(FakeAchievements(emptyList(), listOf(award("missing"))), FakeSessions())()
        assertTrue(result.isFailure)
    }

    @Test
    fun detailQueryPreservesTheCurrentPersonsAwardAndDataSource() = runBlocking {
        val expectedAward = award("earned")
        val getAchievements = GetAchievementsUseCase(
            FakeAchievements(listOf(definition("earned"), definition("other")), listOf(expectedAward)),
            FakeSessions()
        )
        val detail = GetAchievementByIdUseCase(getAchievements)("earned").getOrThrow()
        assertEquals("earned", detail.entry.achievement.id)
        assertEquals(expectedAward, detail.entry.award)
        assertTrue(detail.isSimulated)
    }

    @Test
    fun detailQueryDistinguishesAnUnknownMedalFromAnExpiredSession() = runBlocking {
        val repository = FakeAchievements(listOf(definition("known")), emptyList())
        val available = GetAchievementByIdUseCase(GetAchievementsUseCase(repository, FakeSessions()))
        val expired = GetAchievementByIdUseCase(GetAchievementsUseCase(repository, FakeSessions(null)))
        assertTrue(available("missing").exceptionOrNull() is AchievementNotFoundException)
        assertTrue(expired("missing").exceptionOrNull() is AchievementSessionRequiredException)
    }

    private fun definition(id: String, active: Boolean = true) =
        Achievement(id, "TEST", id, "Eligible action", "INDIVIDUAL", "ECOPOINTS", 10, active)

    private fun award(id: String, userId: Long = 1, scope: String = "INDIVIDUAL") =
        AchievementAward("award-$id", id, scope, userId, "event", "2026-10-08T15:00:00Z", null)

    private class FakeAchievements(val catalog: List<Achievement>, val awards: List<AchievementAward>) : AchievementRepository {
        override val isSimulated = true
        var reads = 0
        var awardsFailure: Throwable? = null
        override suspend fun getCatalog(): Result<List<Achievement>> {
            reads++
            return Result.success(catalog)
        }
        override suspend fun getMyAwards(): Result<List<AchievementAward>> {
            reads++
            return awardsFailure?.let { Result.failure(it) } ?: Result.success(awards)
        }
    }

    private class FakeSessions(session: Session? = validSession()) : SessionRepository {
        private val sessionFlow = MutableStateFlow(session)
        override fun getSession() = sessionFlow
        override suspend fun saveSession(session: Session) { sessionFlow.value = session }
        override suspend fun clearSession() { sessionFlow.value = null }
    }

    companion object {
        private fun validSession() = Session(1, "test@example.com", "demo", Long.MAX_VALUE)
    }
}
