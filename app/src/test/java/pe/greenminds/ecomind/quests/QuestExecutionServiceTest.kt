package pe.greenminds.ecomind.quests

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import pe.greenminds.ecomind.quests.application.service.*
import pe.greenminds.ecomind.quests.domain.entity.*
import pe.greenminds.ecomind.quests.domain.repositories.*
import pe.greenminds.ecomind.quests.domain.valueobject.*

class QuestExecutionServiceTest {
    private val quest = Quest(1, 1, 1, QuestPublicationStatus.PUBLISHED, null, "Save energy", "Switch off lights",
        QuestCategory.ENERGY, QuestType.ACTIVITIES, QuestReward(0, 50), null, 60, QuestTheme.CHECKBOX, null, null)
    private val activity = Activity(1, 1, "Switch off lights", 1, ActivityType.CHECKBOX, emptyMap(), null)
    private val assignment = QuestUser(7, 1, 1, QuestStatus.IN_PROGRESS, 0.0, null)
    private val snapshot get() = QuestExecution(quest, listOf(activity), assignment, listOf(ActivityUser(9, 7, 1, 0.0, null, activity.description, emptyMap())))
    private class FakeRepository(val assignment: QuestUser) : QuestExecutionRepository {
        var starts = 0
        var finishes = 0
        override suspend fun find(questId: Long) = assignment
        override suspend fun start(questId: Long): QuestUser { starts++; return assignment.copy(id = 8, status = QuestStatus.IN_PROGRESS, progress = 0.0, endDate = null) }
        override suspend fun get(id: Long) = assignment
        override suspend fun activities(questId: Long) = emptyList<Activity>()
        override suspend fun activityUsers(questUserId: Long) = emptyList<ActivityUser>()
        override suspend fun check(activityUserId: Long, checked: Boolean): ActivityUser = error("unused")
        override suspend fun finish(questUserId: Long): QuestUser { finishes++; return assignment.copy(status = QuestStatus.COMPLETED) }
    }
    private val catalog = object : QuestRepository {
        override suspend fun getQuest(questId: Long) = Result.success(quest)
        override suspend fun getQuests() = Result.success(listOf(quest))
        override suspend fun searchQuests(query: String, category: QuestCategory?, questType: QuestType?) = Result.success(listOf(quest))
    }
    @Test fun resumesExistingAssignmentWithoutCreatingAnother() = runBlocking {
        val repository = FakeRepository(assignment)
        val result = QuestExecutionService(catalog, repository).start(snapshot)
        assertEquals(7L, result.assignment?.id)
        assertEquals(0, repository.starts)
    }
    @Test fun refusesPrematureFinishWithoutCallingBackend() = runBlocking {
        val repository = FakeRepository(assignment)
        try { QuestExecutionService(catalog, repository).finish(snapshot); fail("Expected unfinished quest to be rejected") }
        catch (_: IllegalStateException) { }
        assertEquals(0, repository.finishes)
    }
    @Test fun completedQuestStartsANewAttemptFromZero() = runBlocking {
        val completed = assignment.copy(status = QuestStatus.COMPLETED, progress = 100.0, endDate = "2026-10-09")
        val repository = FakeRepository(completed)
        val result = QuestExecutionService(catalog, repository).start(snapshot.copy(assignment = completed))
        assertEquals(1, repository.starts)
        assertEquals(8L, result.assignment?.id)
        assertEquals(QuestStatus.IN_PROGRESS, result.assignment?.status)
        assertEquals(0.0, result.assignment!!.progress, 0.0)
        assertNull(result.assignment.endDate)
        assertTrue(result.checks.isEmpty())
        assertEquals(QuestStatus.COMPLETED, completed.status)
    }
    @Test fun finishingNeedsServerReadinessAndEveryAssignedActivity() = runBlocking {
        val repository = FakeRepository(assignment)
        val ready = snapshot.copy(assignment = assignment.copy(status = QuestStatus.READY_TO_COMPLETE, progress = 100.0),
            checks = snapshot.checks.map { it.copy(progress = 100.0) })
        assertFalse(ready.copy(checks = emptyList()).canFinish)
        val result = QuestExecutionService(catalog, repository).finish(ready)
        assertEquals(QuestStatus.COMPLETED, result.assignment?.status)
        assertEquals(1, repository.finishes)
    }
}
