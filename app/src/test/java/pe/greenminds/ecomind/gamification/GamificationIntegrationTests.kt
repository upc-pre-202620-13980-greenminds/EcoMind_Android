package pe.greenminds.ecomind.gamification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.greenminds.ecomind.gamification.application.GetRankingUseCase
import pe.greenminds.ecomind.gamification.domain.model.AchievementGroup
import pe.greenminds.ecomind.gamification.domain.model.RankingParticipant
import pe.greenminds.ecomind.gamification.domain.model.RankingPeriod
import pe.greenminds.ecomind.gamification.domain.model.RankingTransaction
import pe.greenminds.ecomind.gamification.domain.model.RankingType
import pe.greenminds.ecomind.gamification.domain.repositories.RankingRepository
import pe.greenminds.ecomind.gamification.domain.repositories.ShareRequestStore
import pe.greenminds.ecomind.gamification.infrastructure.di.GamificationApiModule
import pe.greenminds.ecomind.gamification.infrastructure.remote.GamificationApi
import pe.greenminds.ecomind.gamification.infrastructure.remote.RemoteProgressRepository
import pe.greenminds.ecomind.iam.domain.model.Session
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.iam.infrastructure.remote.AuthInterceptor
import pe.greenminds.ecomind.shared.infrastructure.di.NetworkModule
import pe.greenminds.ecomind.shared.infrastructure.remote.RemoteAccess
import pe.greenminds.ecomind.users.domain.model.Family
import pe.greenminds.ecomind.users.domain.model.SocialRole
import pe.greenminds.ecomind.users.domain.model.UserProfile
import pe.greenminds.ecomind.users.domain.model.WeeklyReport
import pe.greenminds.ecomind.users.domain.repositories.FamilyRepository
import pe.greenminds.ecomind.users.domain.repositories.ProfileRepository
import pe.greenminds.ecomind.users.interfaces.acl.UsersContextFacade

class GamificationIntegrationTests {
    private val currentAccountId = 101L
    private lateinit var sessions: TestSessions
    private lateinit var server: MockWebServer
    private lateinit var api: GamificationApi
    private lateinit var access: RemoteAccess

    @Before
    fun prepare() {
        sessions = TestSessions()
        server = MockWebServer().apply { start() }
        val client = NetworkModule.provideOkHttpClient(AuthInterceptor(sessions)).newBuilder()
            .addInterceptor { chain ->
                val request = chain.request()
                val query = request.url.encodedQuery?.let { "?$it" } ?: ""
                chain.proceed(request.newBuilder().url(server.url(request.url.encodedPath + query)).build())
            }
            .build()
        api = GamificationApiModule.provideGamificationApi(NetworkModule.provideRetrofit(client))
        access = RemoteAccess(sessions)
    }

    @After
    fun close() {
        server.shutdown()
    }

    @Test
    fun weeklyRankingKeepsCurrentAccountAtItsTruePositionBeyondTopTwenty() = runBlocking {
        val participants = (1L..100L).map {
            RankingParticipant(it, "Participant %03d".format(it), totalEcopoints = 0)
        } + RankingParticipant(currentAccountId, "Current participant", totalEcopoints = 0)
        val transactions = (1L..20L).map {
            RankingTransaction(it, ecopoints = 10, occurredAtMillis = 1L)
        } + RankingTransaction(currentAccountId, ecopoints = 5, occurredAtMillis = 1L)
        val repository = RankingData(participants, transactions)
        val useCase = GetRankingUseCase(sessions, repository, emptyUsersContext())

        val ranking = useCase(RankingType.GLOBAL, RankingPeriod.WEEKLY).getOrThrow()

        assertEquals(20, ranking.entries.size)
        assertEquals((1L..20L).toList(), ranking.entries.map { it.participantId })
        assertFalse(ranking.entries.any { it.isCurrentUser })
        assertEquals(21, ranking.currentEntry?.position)
        assertEquals(currentAccountId, ranking.currentEntry?.participantId)
        assertEquals(5, ranking.currentEntry?.ecopoints)
        assertTrue(ranking.currentEntry?.isCurrentUser == true)
        assertNull(ranking.entries.find { it.participantId == currentAccountId })
        assertEquals(1, repository.participantReads)
        assertEquals(1, repository.transactionReads)
    }

    @Test
    fun familyAchievementAwardsMustBelongToTheRequestedFamily() = runBlocking {
        val wrongFamilyAward = """[{"id":"award-1","achievementId":"medal-1","scope":"FAMILY","beneficiaryId":77,"sourceEventId":"event-1","awardedAt":"2026-10-08T15:00:00Z","communityId":null}]"""
        server.enqueue(jsonResponse(wrongFamilyAward))

        val result = RemoteProgressRepository(api, access, EmptyShares())
            .getGroupAchievements(AchievementGroup(42L, "Family", "FAMILY"))

        assertTrue(result.isFailure)
        val request = server.takeRequest()
        assertEquals("/api/v1/gamification/families/42/achievements?page=0&size=100", request.path)
        assertEquals("Bearer live-token", request.getHeader("Authorization"))
        assertEquals(1, server.requestCount)
    }

    private fun jsonResponse(body: String) = MockResponse()
        .setResponseCode(200)
        .setBody(body)
        .addHeader("Content-Type", "application/json")

    private fun emptyUsersContext() = UsersContextFacade(
        object : ProfileRepository {
            override suspend fun getProfile(userId: Long): Result<UserProfile> = error("Not used")
            override suspend fun getProfiles(): Result<List<UserProfile>> = error("Not used")
            override suspend fun createProfile(userId: Long, name: String, socialRole: SocialRole) = Unit
            override suspend fun spendGems(userId: Long, amount: Int): Result<Int> = error("Not used")
            override suspend fun updateName(userId: Long, name: String): Result<UserProfile> = error("Not used")
        },
        object : FamilyRepository {
            override suspend fun getFamilyOf(userId: Long): Result<Family?> = Result.success(null)
            override suspend fun getWeeklyReport(familyId: Long): Result<WeeklyReport?> = Result.success(null)
        }
    )

    private class RankingData(
        private val participants: List<RankingParticipant>,
        private val transactions: List<RankingTransaction>
    ) : RankingRepository {
        var participantReads = 0
        var transactionReads = 0

        override suspend fun getParticipants(type: RankingType): Result<List<RankingParticipant>> {
            participantReads++
            assertEquals(RankingType.GLOBAL, type)
            return Result.success(participants)
        }

        override suspend fun getTransactions(
            type: RankingType,
            fromMillis: Long,
            toMillis: Long
        ): Result<List<RankingTransaction>> {
            transactionReads++
            assertEquals(RankingType.GLOBAL, type)
            assertTrue(fromMillis < toMillis)
            return Result.success(transactions)
        }
    }

    private class TestSessions : SessionRepository {
        private val session = MutableStateFlow<Session?>(
            Session(101L, "student@example.invalid", "live-token", Long.MAX_VALUE)
        )

        override fun getSession() = session
        override suspend fun saveSession(session: Session) { this.session.value = session }
        override suspend fun clearSession() { session.value = null }
    }

    private class EmptyShares : ShareRequestStore {
        override suspend fun read(user: Long, award: String) = null
        override suspend fun getOrCreate(user: Long, award: String, community: Long) =
            error("Sharing is not used by this test")
        override suspend fun discard(user: Long, award: String, requestId: String) = Unit
    }
}
