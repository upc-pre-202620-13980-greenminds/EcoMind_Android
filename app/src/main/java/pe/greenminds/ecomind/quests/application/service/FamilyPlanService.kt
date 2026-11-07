package pe.greenminds.ecomind.quests.application.service

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.quests.application.FamilyPlanDetails
import pe.greenminds.ecomind.quests.application.FamilyPlanOverview
import pe.greenminds.ecomind.quests.application.FamilyQuestExecution
import pe.greenminds.ecomind.quests.domain.entity.FamilyPlan
import pe.greenminds.ecomind.quests.domain.repositories.FamilyPlanRepository
import pe.greenminds.ecomind.quests.domain.repositories.QuestExecutionRepository
import pe.greenminds.ecomind.quests.domain.repositories.QuestProgressRepository
import pe.greenminds.ecomind.quests.domain.valueobject.ActivityType
import pe.greenminds.ecomind.quests.domain.valueobject.QuestCategory
import pe.greenminds.ecomind.quests.domain.valueobject.QuestTheme
import pe.greenminds.ecomind.quests.domain.valueobject.QuestType
import pe.greenminds.ecomind.users.domain.model.FamilyMembership
import pe.greenminds.ecomind.users.domain.repositories.FamilyMembershipRepository
import javax.inject.Inject

class FamilyPlanService @Inject constructor(
    private val sessions: SessionRepository,
    private val memberships: FamilyMembershipRepository,
    private val plans: FamilyPlanRepository,
    private val progress: QuestProgressRepository,
    private val execution: QuestExecutionRepository
) {
    suspend fun getOverview(): Result<FamilyPlanOverview> = result {
        val member = currentMembership()
        FamilyPlanOverview(member, member?.let { plans.getPlans(it.familyId).getOrThrow() }.orEmpty())
    }

    suspend fun getDetails(planId: Long): Result<FamilyPlanDetails> = result {
        val member = requireNotNull(currentMembership()) { "You must belong to a family" }
        val plan = plans.getPlan(planId).getOrThrow()
        check(plan.familyId == member.familyId) { "This plan belongs to another family" }
        val quests = if (plan.items.isEmpty()) emptyList() else
            progress.getQuests(plan.items.map { it.questId }.toSet()).getOrThrow()
        FamilyPlanDetails(plan, member, quests.associateBy { it.id })
    }

    suspend fun searchQuests(
        query: String = "", category: QuestCategory? = null, theme: QuestTheme? = null
    ) = plans.searchQuests(query, category, theme)

    suspend fun ensureDraft(): Result<FamilyPlan> = result {
        val overview = getOverview().getOrThrow()
        check(overview.isParent) { "Only a family parent can plan challenges" }
        overview.draftPlan ?: plans.createDraft(requireNotNull(overview.membership).familyId, emptyList()).getOrThrow()
    }

    suspend fun addQuest(planId: Long, questId: Long): Result<FamilyPlan> = result {
        val current = editableDraft(planId)
        val ids = current.items.map { it.questId }
        if (questId in ids) current else plans.updateDraft(planId, ids + questId).getOrThrow()
    }

    suspend fun removeQuest(planId: Long, questId: Long): Result<FamilyPlan> = result {
        val current = editableDraft(planId)
        plans.updateDraft(planId, current.items.map { it.questId }.filterNot { it == questId }).getOrThrow()
    }

    suspend fun activate(planId: Long): Result<FamilyPlan> = result {
        check(getDetails(planId).getOrThrow().canActivate) { "The draft must have an owner and at least one challenge" }
        plans.activate(planId).getOrThrow()
    }

    suspend fun complete(planId: Long): Result<FamilyPlan> = result {
        check(getDetails(planId).getOrThrow().canComplete) { "Only the owner can complete a ready plan" }
        plans.complete(planId).getOrThrow()
    }

    suspend fun delete(planId: Long): Result<FamilyPlan> = result {
        check(getDetails(planId).getOrThrow().canDelete) { "This plan cannot be deleted by the current user" }
        plans.delete(planId).getOrThrow()
    }

    suspend fun getExecution(planId: Long, itemId: Long): Result<FamilyQuestExecution> = result {
        val details = getDetails(planId).getOrThrow()
        val item = details.plan.items.single { it.id == itemId }
        val sessionId = requireNotNull(item.collaborativeSessionId) { "Activate the plan before opening its activities" }
        val quest = requireNotNull(details.quests[item.questId]) { "The challenge is unavailable" }
        check(quest.type == QuestType.FAMILY)
        val assignment = progress.getQuestUsers().getOrThrow().singleOrNull {
            it.userId == details.membership.userId && it.questId == item.questId &&
                it.collaborativeSessionId == sessionId
        } ?: error("There is no assignment for the current user in this family plan")
        val activities = execution.activities(item.questId)
        val checks = execution.activityUsers(assignment.id)
        check(checks.all { it.questUserId == assignment.id && it.collaborativeSessionId == sessionId })
        FamilyQuestExecution(details, item, quest, assignment, activities, checks)
    }

    suspend fun checkActivity(
        planId: Long, itemId: Long, activityUserId: Long, checked: Boolean
    ): Result<FamilyQuestExecution> = result {
        val current = getExecution(planId, itemId).getOrThrow()
        check(current.canCheck) { "This family plan is no longer active" }
        val check = current.checks.single { it.id == activityUserId }
        require(current.activities.any { it.id == check.activityId && it.type == ActivityType.CHECKBOX }) {
            "This activity type is not supported yet"
        }
        execution.check(activityUserId, checked)
        getExecution(planId, itemId).getOrThrow()
    }

    private suspend fun editableDraft(planId: Long): FamilyPlan {
        val details = getDetails(planId).getOrThrow()
        check(details.canEdit) { "Only the owner can edit a draft" }
        return details.plan
    }

    private suspend fun currentMembership(): FamilyMembership? {
        val session = requireNotNull(sessions.getSession().first()) { "Sign in to access family plans" }
        check(!session.isExpired(System.currentTimeMillis())) { "The session has expired" }
        return memberships.getMembership(session.accountId).getOrThrow()
    }

    private suspend fun <T> result(action: suspend () -> T): Result<T> = try {
        Result.success(action())
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}
