package pe.greenminds.ecomind.quests.application.usecase

import pe.greenminds.ecomind.quests.application.ProgressEntry
import pe.greenminds.ecomind.quests.application.ProgressOverview
import pe.greenminds.ecomind.quests.domain.repositories.QuestProgressRepository
import pe.greenminds.ecomind.quests.domain.valueobject.QuestStatus
import pe.greenminds.ecomind.quests.domain.valueobject.QuestType
import javax.inject.Inject

class GetProgressOverviewUseCase @Inject constructor(
    private val repository: QuestProgressRepository
) {
    suspend operator fun invoke(): Result<ProgressOverview> {
        val assignments = repository.getQuestUsers().getOrElse { return Result.failure(it) }
        val quests = repository.getQuests().getOrElse { return Result.failure(it) }.associateBy { it.id }
        val activities = repository.getActivityUsers().getOrElse { return Result.failure(it) }
            .groupBy { it.questUserId }
        val active = setOf(QuestStatus.IN_PROGRESS, QuestStatus.READY_TO_COMPLETE)
        val inProgress = mutableListOf<ProgressEntry>()
        val family = mutableListOf<ProgressEntry>()
        val finished = mutableListOf<ProgressEntry>()
        for (assignment in assignments) {
            val quest = quests[assignment.questId] ?: continue
            val entry = ProgressEntry(assignment, quest.title, activities[assignment.id].orEmpty())
            when {
                assignment.status == QuestStatus.COMPLETED -> finished.add(entry)
                assignment.status !in active -> continue
                quest.type == QuestType.FAMILY -> family.add(entry)
                else -> inProgress.add(entry)
            }
        }
        return Result.success(ProgressOverview(inProgress, family, finished))
    }
}
