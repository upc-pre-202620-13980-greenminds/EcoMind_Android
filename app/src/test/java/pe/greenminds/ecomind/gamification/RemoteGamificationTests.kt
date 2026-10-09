package pe.greenminds.ecomind.gamification

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import pe.greenminds.ecomind.gamification.domain.model.*
import pe.greenminds.ecomind.gamification.domain.repositories.*
import pe.greenminds.ecomind.gamification.infrastructure.remote.*
import pe.greenminds.ecomind.iam.domain.model.Session
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.shared.infrastructure.remote.RemoteAccess
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RemoteGamificationTests {
    private lateinit var server: MockWebServer
    private lateinit var api: GamificationApi
    private lateinit var sessions: Sessions
    private lateinit var access: RemoteAccess
    private val awardId = "10000000-0000-0000-0000-000000000001"
    private val achievementId = "00000000-0000-0000-0000-000000000001"

    @Before fun prepare() {
        server = MockWebServer().apply { start() }
        api = Retrofit.Builder().baseUrl(server.url("/api/v1/")).addConverterFactory(GsonConverterFactory.create()).build().create(GamificationApi::class.java)
        sessions = Sessions()
        access = RemoteAccess(sessions)
    }
    @After fun close() { server.shutdown() }
    private fun respond(body: String, code: Int = 200) { server.enqueue(MockResponse().setResponseCode(code).setBody(body).addHeader("Content-Type", "application/json")) }
    private fun catalog(id: Int) = """{"id":"$id","code":"STEPS","name":"Steps","description":"Keep learning","scope":"INDIVIDUAL","metric":"ECOPOINTS","target":10,"active":true}"""

    @Test fun catalogReadsTheFullArrayAndSendsJwt() = runBlocking {
        respond((1..100).joinToString(prefix="[", postfix="]") { catalog(it) })
        respond("[${catalog(101)}]")
        val result = RemoteAchievementRepository(api, access).getCatalog().getOrThrow()
        assertEquals(101, result.size)
        assertEquals("Bearer live-token", server.takeRequest().getHeader("Authorization"))
        assertTrue(server.takeRequest().path!!.contains("page=1"))
    }
    @Test fun rankingFollowsHasNextAndKeepsZeroScores() = runBlocking {
        respond("""{"items":[{"beneficiaryId":1,"displayName":"Ana","totalEcopoints":0}],"page":0,"size":100,"hasNext":true}""")
        respond("""{"items":[{"beneficiaryId":2,"displayName":"Luis","totalEcopoints":15}],"page":1,"size":100,"hasNext":false}""")
        val values = RemoteRankingRepository(api, access).getParticipants(RankingType.GLOBAL).getOrThrow()
        assertEquals(listOf(0,15), values.map { it.totalEcopoints })
    }
    @Test fun invalidPaginationFailsInsteadOfLooping() = runBlocking {
        respond("""{"items":[],"page":0,"size":100,"hasNext":true}""")
        assertTrue(RemoteRankingRepository(api, access).getParticipants(RankingType.GLOBAL).isFailure)
        assertEquals(1, server.requestCount)
    }
    @Test fun unauthorizedIsSessionRecoveryAndDoesNotReturnFixtures() = runBlocking {
        respond("{}", 401)
        val result = RemoteAchievementRepository(api, access).getMyAwards()
        assertTrue(result.exceptionOrNull() is AchievementSessionRequiredException)
    }
    @Test fun demoTokenCannotBeSentToBackend() = runBlocking {
        sessions.value.value = sessions.value.value!!.copy(accessToken = "demo-access-token")
        assertTrue(RemoteAchievementRepository(api, access).getMyAwards().isFailure)
        assertEquals(0, server.requestCount)
    }
    @Test fun switchingAccountDuringRequestDiscardsItsResponse() = runBlocking {
        val result = access.authenticated { _, _ -> sessions.value.value = sessions.value.value!!.copy(accountId = 2); "old private data" }
        assertTrue(result.exceptionOrNull() is AchievementSessionRequiredException)
    }
    @Test fun cancellationIsNotConvertedToAnErrorResult() = runBlocking {
        try { access.authenticated<Unit> { _, _ -> throw CancellationException() }; fail("Cancellation must propagate") }
        catch (_: CancellationException) { }
    }
    @Test fun historyUsesGrantedAmountAndUtcPeriod() = runBlocking {
        respond("""{"items":[{"id":"$awardId","source":{"type":"QUEST","executionId":"$achievementId"},"beneficiary":{"type":"USER","id":1},"baseReward":{"ecopoints":10,"gems":2},"grantedReward":{"ecopoints":20,"gems":2},"occurredAt":"2026-10-08T15:00:00Z"}],"page":0,"size":100,"hasNext":false}""")
        val data = RemoteProgressRepository(api, access, Shares()).getHistory(0, 1000).getOrThrow()
        assertEquals(20L, data.single().ecopoints)
        val url = server.takeRequest().requestUrl!!
        assertEquals("1970-01-01T00:00:00Z", url.queryParameter("from"))
        assertEquals("1970-01-01T00:00:01Z", url.queryParameter("to"))
    }
    @Test fun historyRejectsAnotherBeneficiary() = runBlocking {
        respond("""{"items":[{"id":"$awardId","source":{"type":"QUEST","executionId":"$achievementId"},"beneficiary":{"type":"USER","id":2},"baseReward":{"ecopoints":10,"gems":2},"grantedReward":{"ecopoints":20,"gems":2},"occurredAt":"2026-10-08T15:00:00Z"}],"page":0,"size":100,"hasNext":false}""")
        assertTrue(RemoteProgressRepository(api, access, Shares()).getHistory(0, 1000).isFailure)
    }
    @Test fun sharingRetriesTheSameRequestAfterUncertainResponse() = runBlocking {
        val repository = RemoteProgressRepository(api, access, Shares())
        respond("{}", 503)
        assertTrue(repository.share(awardId, 9).isFailure)
        val initial = server.takeRequest().body.readUtf8()
        respond("""{"requestId":"30000000-0000-0000-0000-000000000001","awardId":"$awardId","requestedBy":1,"communityId":9,"status":"PENDING","publicationId":null,"createdAt":"2026-10-08T15:00:00Z","confirmedAt":null}""", 202)
        val result = repository.share(awardId, 9).getOrThrow()
        assertEquals(initial, server.takeRequest().body.readUtf8())
        assertEquals("PENDING", result.status)
        assertNull(result.publicationId)
    }
    @Test fun lostRequestCanRecoverWithoutInventingPublication() = runBlocking {
        val shares = Shares()
        shares.getOrCreate(1, awardId, 9)
        respond("{}", 404)
        val status = RemoteProgressRepository(api, access, shares).getShare(awardId).getOrThrow()!!
        assertEquals("UNSENT", status.status)
        assertNull(status.publicationId)
    }
    @Test fun collectiveAchievementsRespectServerMembershipDenial() = runBlocking {
        respond("{}", 403)
        assertTrue(RemoteProgressRepository(api, access, Shares()).getGroupAchievements(AchievementGroup(9,"Group","COMMUNITY")).isFailure)
        assertEquals(1,server.requestCount)
    }
    @Test fun communityAwardsUseCommunityIdWithoutIndividualBeneficiary() = runBlocking {
        respond("""[{"id":"$awardId","achievementId":"$achievementId","scope":"COMMUNITY","beneficiaryId":null,"sourceEventId":"$awardId","awardedAt":"2026-10-08T15:00:00Z","communityId":9}]""")
        respond("""[{"id":"$achievementId","code":"GOAL","name":"Goal met","description":"Together","scope":"COMMUNITY","metric":"COMPLETED_COMMUNITY_GOALS","target":1,"active":true}]""")
        val data = RemoteProgressRepository(api, access, Shares()).getGroupAchievements(AchievementGroup(9,"Garden","COMMUNITY")).getOrThrow()
        assertEquals(9L, data.earned.single().award!!.communityId)
        assertNull(data.earned.single().award!!.beneficiaryId)
    }
    @Test fun timestampHandlesOffsetsFractionsAndRejectsInvalidInput() {
        assertEquals(instantToMillis("2026-10-08T15:00:00.123Z"), instantToMillis("2026-10-08T10:00:00.123456-05:00"))
        try { instantToMillis("2026-02-31T15:00:00Z"); fail("Invalid date") } catch (_: Exception) { }
    }
    private class Sessions : SessionRepository {
        val value = MutableStateFlow<Session?>(Session(1,"test@example.com","live-token",System.currentTimeMillis()+600000))
        override fun getSession() = value
        override suspend fun saveSession(session: Session) { value.value = session }
        override suspend fun clearSession() { value.value = null }
    }
    private class Shares : ShareRequestStore {
        val requests = mutableMapOf<Pair<Long,String>, ShareRequest>()
        override suspend fun read(user: Long, award: String) = requests[user to award]
        override suspend fun getOrCreate(user: Long, award: String, community: Long) = requests.getOrPut(user to award) {
            ShareRequest("30000000-0000-0000-0000-000000000001", award, community)
        }.also { check(it.communityId == community) }
    }
}
