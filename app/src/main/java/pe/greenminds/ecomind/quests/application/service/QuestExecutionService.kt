package pe.greenminds.ecomind.quests.application.service

import javax.inject.Inject
import pe.greenminds.ecomind.quests.domain.entity.*
import pe.greenminds.ecomind.quests.domain.repositories.*
import pe.greenminds.ecomind.quests.domain.valueobject.*

data class QuestExecution(val quest: Quest, val activities: List<Activity>, val assignment: QuestUser?, val checks: List<ActivityUser>) {
    val supported get() = quest.type == QuestType.ACTIVITIES && quest.theme == QuestTheme.CHECKBOX && activities.isNotEmpty() && activities.all { it.type == ActivityType.CHECKBOX }
    val canFinish get() = supported && assignment?.status == QuestStatus.READY_TO_COMPLETE && assignment.progress >= 100 && activities.all { a -> checks.any { it.activityId == a.id && it.progress >= 100 } }
}

class QuestExecutionService @Inject constructor(private val quests: QuestRepository, private val repository: QuestExecutionRepository) {
    suspend fun load(id: Long): QuestExecution {
        val quest = quests.getQuest(id).getOrThrow()
        val activities = repository.activities(id)
        val assignment = repository.find(id)
        return QuestExecution(quest, activities, assignment, assignment?.let { repository.activityUsers(it.id) }.orEmpty())
    }
    suspend fun start(current: QuestExecution): QuestExecution {
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
        require(current.checks.any { it.id == activityUserId })
        repository.check(activityUserId, checked)
        return current.copy(assignment = repository.get(requireNotNull(current.assignment).id), checks = repository.activityUsers(current.assignment.id))
    }
    suspend fun finish(current: QuestExecution): QuestExecution {
        check(current.canFinish)
        return current.copy(assignment = repository.finish(requireNotNull(current.assignment).id))
    }
}
