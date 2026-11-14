package pe.greenminds.ecomind.quests

import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import pe.greenminds.ecomind.gamification.infrastructure.remote.GamificationApi
import pe.greenminds.ecomind.iam.domain.model.Session
import pe.greenminds.ecomind.quests.application.service.QuestExecution
import pe.greenminds.ecomind.quests.domain.entity.Quest
import pe.greenminds.ecomind.quests.domain.entity.QuestUser
import pe.greenminds.ecomind.quests.domain.valueobject.*
import pe.greenminds.ecomind.quests.infrastructure.offline.ConfirmedQuestReward
import pe.greenminds.ecomind.quests.infrastructure.offline.OfflineQuest
import pe.greenminds.ecomind.quests.infrastructure.offline.OfflineQuestStatus
import pe.greenminds.ecomind.quests.infrastructure.offline.syncQuest
import pe.greenminds.ecomind.quests.infrastructure.remote.QuestExecutionApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class OfflineQuestSyncTest {
    private val session = Session(42, "test@example.com", "token", Long.MAX_VALUE)
    private val quest = Quest(
        7, 7, 1, QuestPublicationStatus.PUBLISHED, null, "Checklist", "Description",
        QuestCategory.ENERGY, QuestType.ACTIVITIES, QuestReward(2, 20), null, null,
        QuestTheme.entries.first(), LocalDate.of(2026, 11, 13), null
    )
    private val queued = OfflineQuest(
        QuestExecution(quest, emptyList(), QuestUser(15, 42, 7, QuestStatus.READY_TO_COMPLETE, 100.0, null), emptyList()),
        changes = mapOf(21L to true),
        status = OfflineQuestStatus.PENDING
    )

    @Test
    fun `retries an interrupted completion against its existing assignment`() = runBlocking {
        MockWebServer().use { server ->
            val retrofit = retrofit(server)
            val quests = retrofit.create(QuestExecutionApi::class.java)
            val rewards = retrofit.create(GamificationApi::class.java)
            server.enqueue(json(assignment("COMPLETED")))
            server.enqueue(json("[]"))
            server.enqueue(json(rewardPage("")))

            val awaitingReward = syncQuest(queued, session, quests, rewards)
            assertEquals(OfflineQuestStatus.PENDING, awaitingReward.status)
            assertNull(awaitingReward.reward)
            assertTrue(awaitingReward.changes.isEmpty())
            repeat(3) { assertEquals("GET", server.takeRequest().method) }

            server.enqueue(json(assignment("COMPLETED")))
            val executionId = UUID.nameUUIDFromBytes("quest-user:15".toByteArray())
            server.enqueue(json(rewardPage(
                """{"source":{"type":"QUEST","executionId":"$executionId"},"grantedReward":{"ecopoints":37,"gems":4}}"""
            )))
            val confirmed = syncQuest(awaitingReward, session, quests, rewards)
            assertEquals(OfflineQuestStatus.CONFIRMED, confirmed.status)
            assertEquals(ConfirmedQuestReward(37, 4), confirmed.reward)
            repeat(2) { assertEquals("GET", server.takeRequest().method) }
        }
    }

    @Test
    fun `uploads only missing checklist changes before completing`() = runBlocking {
        MockWebServer().use { server ->
            val retrofit = retrofit(server)
            server.enqueue(json(assignment("IN_PROGRESS")))
            server.enqueue(json("[${activity(0)}]"))
            server.enqueue(json(activity(100)))
            server.enqueue(json(assignment("READY_TO_COMPLETE")))
            server.enqueue(json("[${activity(100)}]"))
            server.enqueue(json(assignment("COMPLETED")))
            server.enqueue(json(rewardPage("")))

            val updated = syncQuest(
                queued, session, retrofit.create(QuestExecutionApi::class.java), retrofit.create(GamificationApi::class.java)
            )
            assertEquals(QuestStatus.COMPLETED, updated.execution.assignment?.status)
            assertEquals(OfflineQuestStatus.PENDING, updated.status)
            val requests = List(7) { server.takeRequest() }
            assertEquals(listOf("/activity-users/21/submit", "/quest-users/15/complete"),
                requests.filter { it.method == "POST" }.map { it.path })
            assertTrue(requests[2].body.readUtf8().contains("\"checked\":true"))
            assertTrue(requests.last().path.orEmpty().startsWith("/gamification/rewards?"))
        }
    }

    private fun retrofit(server: MockWebServer): Retrofit =
        Retrofit.Builder().baseUrl(server.url("/")).addConverterFactory(GsonConverterFactory.create()).build()

    private fun json(body: String) = MockResponse().setHeader("Content-Type", "application/json").setBody(body)

    private fun assignment(status: String): String {
        val progress = if (status == "IN_PROGRESS") 0 else 100
        val end = if (status == "COMPLETED") "\"2026-11-13\"" else "null"
        return """{"id":15,"userId":42,"questId":7,"status":"$status","progress":$progress,"endDate":$end}"""
    }

    private fun activity(progress: Int) = """{"id":21,"questUserId":15,"activityId":8,"progress":$progress}"""

    private fun rewardPage(items: String) = """{"items":[$items],"page":0,"size":100,"hasNext":false}"""
}
