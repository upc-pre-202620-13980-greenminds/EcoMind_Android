package pe.greenminds.ecomind.quests

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import pe.greenminds.ecomind.iam.domain.model.Session
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.quests.application.service.FamilyPlanService
import pe.greenminds.ecomind.quests.domain.entity.*
import pe.greenminds.ecomind.quests.domain.repositories.*
import pe.greenminds.ecomind.quests.domain.valueobject.*
import pe.greenminds.ecomind.users.domain.model.*
import pe.greenminds.ecomind.users.domain.repositories.FamilyMembershipRepository

class FamilyPlanServiceTest {
    private class Fixture {
        var member: FamilyMembership? = FamilyMembership(8, 2, 20, FamilyRole.PARENT)
        var plan = FamilyPlan(9, 2, 20, FamilyPlanStatus.ACTIVE, 0.0, listOf(FamilyPlanItem(11, 3, 45, 0.0)))
        val quest = Quest(3, 3, 1, QuestPublicationStatus.PUBLISHED, null, "Save water", "Family challenge",
            QuestCategory.WATER, QuestType.FAMILY, QuestReward(3, 50), null, 10, QuestTheme.CHECKBOX, null, null)
        var assignments = listOf(
            QuestUser(70, 20, 3, QuestStatus.IN_PROGRESS, 0.0, null, 45),
            QuestUser(99, 20, 3, QuestStatus.COMPLETED, 100.0, null, 88)
        )
        var check = ActivityUser(80, 70, 4, 0.0, null, "Turn off the tap", emptyMap(), 45)
        var creates = 0
        var mutations = 0
        var checks = 0
        var starts = 0
        var finishes = 0
        var listed = 0
        var readAssignment: Long? = null

        private val sessions = object : SessionRepository {
            override fun getSession() = flowOf(Session(20, "test@example.com", "token", Long.MAX_VALUE))
            override suspend fun saveSession(session: Session) = Unit
            override suspend fun clearSession() = Unit
        }
        private val memberships = object : FamilyMembershipRepository {
            override suspend fun getMembership(userId: Long) = Result.success(member)
        }
        private val plans = object : FamilyPlanRepository {
            override suspend fun getPlans(familyId: Long): Result<List<FamilyPlan>> { listed++; return Result.success(listOf(plan)) }
            override suspend fun getActivePlan(familyId: Long) = Result.success(plan)
            override suspend fun getPlan(planId: Long) = Result.success(plan)
            override suspend fun createDraft(familyId: Long, questIds: List<Long>): Result<FamilyPlan> {
                creates++; plan = plan.copy(status = FamilyPlanStatus.DRAFT, items = emptyList()); return Result.success(plan)
            }
            override suspend fun updateDraft(planId: Long, questIds: List<Long>): Result<FamilyPlan> {
                mutations++; plan = plan.copy(items = questIds.map { FamilyPlanItem(it, it, null, 0.0) }); return Result.success(plan)
            }
            override suspend fun activate(planId: Long): Result<FamilyPlan> { mutations++; return Result.success(plan) }
            override suspend fun complete(planId: Long): Result<FamilyPlan> { mutations++; return Result.success(plan) }
            override suspend fun delete(planId: Long): Result<FamilyPlan> { mutations++; return Result.success(plan) }
            override suspend fun searchQuests(query: String, category: QuestCategory?, theme: QuestTheme?) = Result.success(listOf(quest))
        }
        private val progress = object : QuestProgressRepository {
            override suspend fun getQuestUsers() = Result.success(assignments)
            override suspend fun getQuests(questIds: Set<Long>) = Result.success(listOf(quest))
            override suspend fun getActivityUsers(questUserId: Long) = Result.success(listOf(check))
        }
        private val execution = object : QuestExecutionRepository {
            override suspend fun find(questId: Long) = assignments.last()
            override suspend fun start(questId: Long): QuestUser { starts++; error("Family plans must not start a solo attempt") }
            override suspend fun finish(questUserId: Long): QuestUser { finishes++; error("Completion belongs to the plan") }
            override suspend fun get(id: Long) = assignments.single { it.id == id }
            override suspend fun activities(questId: Long) = listOf(Activity(4, 3, "Turn off the tap", 1, ActivityType.CHECKBOX, emptyMap(), null))
            override suspend fun activityUsers(questUserId: Long): List<ActivityUser> { readAssignment = questUserId; return listOf(check) }
            override suspend fun check(activityUserId: Long, checked: Boolean): ActivityUser {
                checks++
                check = check.copy(progress = if (checked) 100.0 else 0.0)
                plan = plan.copy(progress = check.progress, items = plan.items.map { it.copy(progress = check.progress) })
                assignments = assignments.map { if (it.id == 70L) it.copy(progress = check.progress,
                    status = if (checked) QuestStatus.READY_TO_COMPLETE else QuestStatus.IN_PROGRESS) else it }
                return check
            }
        }
        val service = FamilyPlanService(sessions, memberships, plans, progress, execution)
    }

    @Test fun noFamilyIsAnEmptyOverviewWithoutPlanRequests() = runBlocking {
        val fixture = Fixture().apply { member = null }
        val overview = fixture.service.getOverview().getOrThrow()
        assertNull(overview.membership)
        assertTrue(overview.plans.isEmpty())
        assertEquals(0, fixture.listed)
    }

    @Test fun reopeningAnExistingDraftDoesNotCreateAnother() = runBlocking {
        val fixture = Fixture().apply { plan = plan.copy(status = FamilyPlanStatus.DRAFT) }
        assertEquals(9L, fixture.service.ensureDraft().getOrThrow().id)
        assertEquals(0, fixture.creates)
    }

    @Test fun childCanReadButCannotCreateOrClosePlans() = runBlocking {
        val fixture = Fixture().apply { member = member!!.copy(familyRole = FamilyRole.CHILD) }
        assertTrue(fixture.service.getDetails(9).isSuccess)
        assertFalse(fixture.service.getDetails(9).getOrThrow().canManage)
        assertTrue(fixture.service.ensureDraft().isFailure)
        assertTrue(fixture.service.complete(9).isFailure)
        assertEquals(0, fixture.creates + fixture.mutations)
    }

    @Test fun anotherParentIsNotTreatedAsTheOwner() = runBlocking {
        val fixture = Fixture().apply { plan = plan.copy(ownerUserId = 30, status = FamilyPlanStatus.DRAFT) }
        assertTrue(fixture.service.addQuest(9, 3).isFailure)
        assertTrue(fixture.service.delete(9).isFailure)
        assertEquals(0, fixture.mutations)
    }

    @Test fun planFromAnotherFamilyIsRejected() = runBlocking {
        val fixture = Fixture().apply { plan = plan.copy(familyId = 100) }
        assertTrue(fixture.service.getDetails(9).isFailure)
    }

    @Test fun duplicateAddsDoNotSendAnotherUpdate() = runBlocking {
        val fixture = Fixture().apply { plan = plan.copy(status = FamilyPlanStatus.DRAFT) }
        fixture.service.addQuest(9, 3).getOrThrow()
        assertEquals(0, fixture.mutations)
    }

    @Test fun executionSelectsThePlanSessionInsteadOfTheLatestQuestAttempt() = runBlocking {
        val fixture = Fixture()
        val execution = fixture.service.getExecution(9, 11).getOrThrow()
        assertEquals(70L, execution.assignment.id)
        assertEquals(70L, fixture.readAssignment)
        assertEquals(0, fixture.starts + fixture.finishes)
    }

    @Test fun missingMatchingSessionNeverFallsBackToAnotherAttempt() = runBlocking {
        val fixture = Fixture().apply { assignments = assignments.filter { it.collaborativeSessionId == 88L } }
        assertTrue(fixture.service.getExecution(9, 11).isFailure)
        assertNull(fixture.readAssignment)
    }

    @Test fun submittingACheckboxReloadsTheSharedPlanAndDoesNotFinishIndividually() = runBlocking {
        val fixture = Fixture()
        val execution = fixture.service.checkActivity(9, 11, 80, true).getOrThrow()
        assertEquals(100.0, execution.details.plan.progress, 0.0)
        assertTrue(execution.readyToReturnToPlan)
        assertTrue(execution.details.canComplete)
        assertEquals(1, fixture.checks)
        assertEquals(0, fixture.starts + fixture.finishes)
    }

    @Test fun unknownCheckboxAndCancelledPlansDoNotSubmit() = runBlocking {
        val fixture = Fixture()
        assertTrue(fixture.service.checkActivity(9, 11, 999, true).isFailure)
        fixture.plan = fixture.plan.copy(status = FamilyPlanStatus.CANCELLED)
        assertTrue(fixture.service.checkActivity(9, 11, 80, true).isFailure)
        assertEquals(0, fixture.checks)
    }

    @Test fun unfinishedOrEmptyPlanCannotComplete() = runBlocking {
        val fixture = Fixture()
        assertTrue(fixture.service.complete(9).isFailure)
        fixture.plan = fixture.plan.copy(items = emptyList(), progress = 100.0)
        assertTrue(fixture.service.complete(9).isFailure)
        assertEquals(0, fixture.mutations)
    }
}
