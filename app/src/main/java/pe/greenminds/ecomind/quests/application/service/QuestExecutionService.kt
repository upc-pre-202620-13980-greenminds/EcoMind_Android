package pe.greenminds.ecomind.quests.application.service

import javax.inject.Inject
import pe.greenminds.ecomind.quests.domain.entity.*
import pe.greenminds.ecomind.quests.domain.repositories.*
import pe.greenminds.ecomind.quests.domain.valueobject.*

data class QuestExecution(val quest: Quest, val activities: List<Activity>, val assignment: QuestUser?, val checks: List<ActivityUser>,
    val familyPlanId: Long? = null) {
    val supported get() = activities.isNotEmpty() && when (quest.type) {
        QuestType.ACTIVITIES -> true
        QuestType.COLLABORATIVE -> activities.all { it.type == ActivityType.CHECKBOX }
        QuestType.FAMILY -> familyPlanId != null && activities.all { it.type == ActivityType.CHECKBOX }
        else -> false
    }
    val hasWrittenActivities get() = activities.any { it.type == ActivityType.WRITE }
    val canWrite get() = quest.type == QuestType.ACTIVITIES && assignment?.collaborativeSessionId == null &&
        assignment?.status in listOf(QuestStatus.IN_PROGRESS, QuestStatus.READY_TO_COMPLETE)
    val canFinish get() = quest.type != QuestType.FAMILY && supported && assignment?.status == QuestStatus.READY_TO_COMPLETE && assignment.progress >= 100 && activities.all { a -> checks.any { it.activityId == a.id && it.progress >= 100 } }
}

class QuestExecutionService @Inject constructor(private val quests: QuestRepository, private val repository: QuestExecutionRepository) {
    suspend fun load(id: Long): QuestExecution {
        val quest = quests.getQuest(id).getOrThrow()
        if (quest.type == QuestType.MINIGAME) return QuestExecution(quest, emptyList(), null, emptyList())
        val activities = repository.activities(id)
        val assignment = repository.find(id)
        return QuestExecution(quest, activities, assignment, assignment?.let { repository.activityUsers(it.id) }.orEmpty())
    }
    suspend fun start(current: QuestExecution): QuestExecution {
        check(current.quest.type != QuestType.FAMILY)
        check(current.supported)
        val previous = repository.find(current.quest.id)
        val assignment = previous?.takeIf {
            it.status in listOf(QuestStatus.IN_PROGRESS, QuestStatus.READY_TO_COMPLETE)
        } ?: repository.start(current.quest.id)
        check(assignment.status in listOf(QuestStatus.IN_PROGRESS, QuestStatus.READY_TO_COMPLETE))
        return current.copy(assignment = assignment, checks = repository.activityUsers(assignment.id))
    }
    suspend fun check(current: QuestExecution, activityUserId: Long, checked: Boolean): QuestExecution {
        check(current.assignment?.status in listOf(QuestStatus.IN_PROGRESS, QuestStatus.READY_TO_COMPLETE))
        require(current.checks.any { assigned -> assigned.id == activityUserId &&
            current.activities.any { it.id == assigned.activityId && it.type == ActivityType.CHECKBOX } })
        repository.check(activityUserId, checked)
        return current.copy(assignment = repository.get(requireNotNull(current.assignment).id), checks = repository.activityUsers(current.assignment.id))
    }

    suspend fun submitWrittenResponse(current: QuestExecution, activityUserId: Long, text: String): QuestExecution {
        check(current.canWrite)
        val assignment = requireNotNull(current.assignment)
        val assigned = current.checks.single { it.id == activityUserId && it.questUserId == assignment.id }
        require(assigned.collaborativeSessionId == null)
        require(current.activities.any { it.id == assigned.activityId && it.type == ActivityType.WRITE })
        require(assigned.acceptsWrittenResponse(text))
        val saved = repository.submitWrittenResponse(activityUserId, text.trim { Character.isWhitespace(it) })
        return current.copy(
            assignment = repository.get(assignment.id),
            checks = current.checks.map { if (it.id == saved.id) saved else it }
        )
    }
    suspend fun finish(current: QuestExecution): QuestExecution {
        check(current.canFinish)
        return current.copy(assignment = repository.finish(requireNotNull(current.assignment).id))
    }
}
