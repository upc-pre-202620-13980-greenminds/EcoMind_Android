package pe.greenminds.ecomind.gamification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import pe.greenminds.ecomind.gamification.application.GetRankingUseCase
import pe.greenminds.ecomind.gamification.domain.model.*
import pe.greenminds.ecomind.gamification.domain.repositories.RankingRepository
import pe.greenminds.ecomind.iam.domain.model.Session
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.users.domain.model.*
import pe.greenminds.ecomind.users.domain.repositories.*
import pe.greenminds.ecomind.users.interfaces.acl.UsersContextFacade

class RankingSessionTests {
    private val session = Session(1L, "student@example.invalid", "token", Long.MAX_VALUE)

    @Test fun periodRankingUsesGrantedTransactionsAndHighlightsCurrentAccount() = runBlocking {
        val ranking = query(Sessions(session), Rankings())(RankingType.GLOBAL, RankingPeriod.DAILY).getOrThrow()
        assertEquals(14, ranking.currentEntry?.ecopoints)
        assertEquals(1L, ranking.currentEntry?.participantId)
    }

    @Test fun accountSwitchBetweenParticipantsAndTransactionsRejectsCombinedData() = runBlocking {
        val sessions = Sessions(session)
        val repository = Rankings { sessions.value.value = session.copy(accountId = 2L, accessToken = "new-token") }
        val result = query(sessions, repository)(RankingType.GLOBAL, RankingPeriod.DAILY)
        assertTrue(result.exceptionOrNull() is AchievementSessionRequiredException)
    }

    @Test fun expiredSessionCannotReadRanking() = runBlocking {
        val repository = Rankings()
        val result = query(Sessions(session.copy(expiresAtMillis = 0)), repository)(RankingType.GLOBAL, RankingPeriod.ALL_TIME)
        assertTrue(result.exceptionOrNull() is AchievementSessionRequiredException)
        assertEquals(0, repository.reads)
    }

    private fun query(sessions: Sessions, rankings: Rankings): GetRankingUseCase {
        val profiles = object : ProfileRepository {
            override suspend fun getProfile(userId: Long): Result<UserProfile> = error("Not used")
            override suspend fun createProfile(userId: Long, name: String, socialRole: SocialRole) = Unit
            override suspend fun spendGems(userId: Long, amount: Int): Result<Int> = error("Not used")
        }
        val families = object : FamilyRepository {
            override suspend fun getFamilyOf(userId: Long): Result<Family?> = Result.success(null)
            override suspend fun getWeeklyReport(familyId: Long): Result<WeeklyReport?> = Result.success(null)
        }
        return GetRankingUseCase(sessions, rankings, UsersContextFacade(profiles, families))
    }

    private class Sessions(session: Session?) : SessionRepository {
        val value = MutableStateFlow(session)
        override fun getSession() = value
        override suspend fun saveSession(session: Session) { value.value = session }
        override suspend fun clearSession() { value.value = null }
    }

    private class Rankings(private val afterParticipants: () -> Unit = {}) : RankingRepository {
        var reads = 0
        override suspend fun getParticipants(type: RankingType): Result<List<RankingParticipant>> {
            reads++
            afterParticipants()
            return Result.success(listOf(RankingParticipant(1L, "Student", 100)))
        }
        override suspend fun getTransactions(type: RankingType, fromMillis: Long, toMillis: Long) =
            Result.success(listOf(RankingTransaction(1L, 14, fromMillis)))
    }
}
