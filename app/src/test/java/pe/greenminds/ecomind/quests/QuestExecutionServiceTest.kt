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
        override suspend fun submitWrittenResponse(activityUserId: Long, text: String): ActivityUser = error("unused")
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

    @Test fun dailyQuestCanStartAndCompleteItsCheckboxActivity() = runBlocking {
        val daily = snapshot.copy(quest = quest.copy(type = QuestType.DAILY_QUEST), assignment = null, checks = emptyList())
        var check = snapshot.checks.single()
        var currentAssignment = assignment
        val repository = object : QuestExecutionRepository {
            override suspend fun find(questId: Long): QuestUser? = null
            override suspend fun start(questId: Long) = currentAssignment
            override suspend fun get(id: Long) = currentAssignment
            override suspend fun activities(questId: Long) = daily.activities
            override suspend fun activityUsers(questUserId: Long) = listOf(check)
            override suspend fun check(activityUserId: Long, checked: Boolean): ActivityUser {
                assertEquals(check.id, activityUserId)
                assertTrue(checked)
                check = check.copy(progress = 100.0)
                currentAssignment = currentAssignment.copy(status = QuestStatus.READY_TO_COMPLETE, progress = 100.0)
                return check
            }
            override suspend fun submitWrittenResponse(activityUserId: Long, text: String): ActivityUser = error("unused")
            override suspend fun finish(questUserId: Long) = currentAssignment.copy(status = QuestStatus.COMPLETED)
        }
        val service = QuestExecutionService(catalog, repository)

        assertTrue(daily.supported)
        assertFalse(daily.copy(activities = emptyList()).supported)
        val started = service.start(daily)
        assertEquals(QuestStatus.IN_PROGRESS, started.assignment?.status)
        assertFalse(started.canFinish)
        val ready = service.check(started, started.checks.single().id, true)
        assertTrue(ready.canFinish)
        assertEquals(QuestStatus.COMPLETED, service.finish(ready).assignment?.status)
    }

    @Test fun mixedQuestRequiresBothCheckboxesAndAValidWrittenAnswer() = runBlocking {
        val secondCheckbox = activity.copy(id = 2, order = 2)
        val write = activity.copy(id = 3, order = 3, type = ActivityType.WRITE)
        val first = snapshot.checks.single().copy(progress = 100.0)
        val second = first.copy(id = 10, activityId = secondCheckbox.id, progress = 0.0)
        val written = first.copy(id = 11, activityId = write.id, progress = 0.0,
            configuration = mapOf("minCharacters" to 5.0))
        var stored = written
        var checkbox = second
        val repository = object : QuestExecutionRepository by FakeRepository(assignment) {
            override suspend fun submitWrittenResponse(activityUserId: Long, text: String): ActivityUser {
                assertEquals(written.id, activityUserId)
                stored = written.copy(progress = 100.0, responseText = text)
                return stored
            }

            override suspend fun check(activityUserId: Long, checked: Boolean): ActivityUser {
                assertEquals(second.id, activityUserId)
                checkbox = second.copy(progress = if (checked) 100.0 else 0.0)
                return checkbox
            }

            override suspend fun activityUsers(questUserId: Long) = listOf(first, checkbox, stored)

            override suspend fun get(id: Long): QuestUser {
                val progress = activityUsers(id).map { it.progress }.average()
                return assignment.copy(progress = progress, status = if (progress == 100.0)
                    QuestStatus.READY_TO_COMPLETE else QuestStatus.IN_PROGRESS)
            }
        }
        val service = QuestExecutionService(catalog, repository)
        val mixed = snapshot.copy(quest = quest.copy(theme = QuestTheme.WRITE),
            activities = listOf(activity, secondCheckbox, write), checks = listOf(first, second, written))
        assertTrue(mixed.supported)
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { service.submitWrittenResponse(mixed, written.id, "  abc  ") }
        }
        assertNull(stored.responseText)
        val withAnswer = service.submitWrittenResponse(mixed, written.id, "  Ahorrar agua  ")
        assertEquals("Ahorrar agua", withAnswer.checks.last().responseText)
        assertFalse(withAnswer.canFinish)
        assertTrue(service.check(withAnswer, second.id, true).canFinish)
    }

    @Test fun familyQuestUsesTheExistingUiWithoutStartingASoloAttempt() = runBlocking {
        val repository = FakeRepository(assignment)
        val family = snapshot.copy(quest = quest.copy(type = QuestType.FAMILY), familyPlanId = 9)
        assertTrue(family.supported)
        assertFalse(family.copy(familyPlanId = null).supported)
        try { QuestExecutionService(catalog, repository).start(family); fail("Family activation already created the assignment") }
        catch (_: IllegalStateException) { }
        assertEquals(0, repository.starts)
    }

    @Test fun readyFamilyQuestCannotBeCompletedThroughTheIndividualEndpoint() = runBlocking {
        val repository = FakeRepository(assignment)
        val family = snapshot.copy(quest = quest.copy(type = QuestType.FAMILY), familyPlanId = 9,
            assignment = assignment.copy(status = QuestStatus.READY_TO_COMPLETE, progress = 100.0),
            checks = snapshot.checks.map { it.copy(progress = 100.0) })
        assertFalse(family.canFinish)
        try { QuestExecutionService(catalog, repository).finish(family); fail("Completion belongs to the family plan") }
        catch (_: IllegalStateException) { }
        assertEquals(0, repository.finishes)
    }
}
