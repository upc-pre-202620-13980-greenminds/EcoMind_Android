package pe.greenminds.ecomind.quests.application.usecase

import pe.greenminds.ecomind.quests.application.ProgressEntry
import pe.greenminds.ecomind.quests.application.ProgressOverview
import pe.greenminds.ecomind.quests.application.groupQuestCompletions
import pe.greenminds.ecomind.quests.domain.repositories.QuestProgressRepository
import pe.greenminds.ecomind.quests.domain.valueobject.QuestStatus
import pe.greenminds.ecomind.quests.domain.valueobject.QuestType
import javax.inject.Inject

class GetProgressOverviewUseCase @Inject constructor(
    private val repository: QuestProgressRepository
) {
    suspend operator fun invoke(accountId: Long? = null): Result<ProgressOverview> {
        val assignments = repository.getQuestUsers().getOrElse { return Result.failure(it) }
        if (accountId != null && assignments.any { it.userId != accountId }) {
            return Result.failure(IllegalStateException("Progress belongs to another account"))
        }
        val quests = repository.getQuests(assignments.map { it.questId }.toSet())
            .getOrElse { return Result.failure(it) }.associateBy { it.id }
        val active = setOf(QuestStatus.IN_PROGRESS, QuestStatus.READY_TO_COMPLETE)
        val inProgress = mutableListOf<ProgressEntry>()
        val family = mutableListOf<ProgressEntry>()
        val finished = mutableListOf<ProgressEntry>()
        for (assignment in assignments) {
            val quest = quests[assignment.questId]
            val entry = ProgressEntry(assignment, quest?.title.orEmpty(), questType = quest?.type)
            when {
                assignment.status == QuestStatus.COMPLETED -> finished.add(entry)
                assignment.status !in active -> continue
                quest?.type == QuestType.FAMILY -> family.add(entry)
                else -> inProgress.add(entry)
            }
        }
        val completedQuests = groupQuestCompletions(finished)
        return Result.success(ProgressOverview(
            inProgress.sortedByDescending { it.questUser.id },
            family.sortedByDescending { it.questUser.id },
            completedQuests,
            completedQuests
        ))
    }
}
