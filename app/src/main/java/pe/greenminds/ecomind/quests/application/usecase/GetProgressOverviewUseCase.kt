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
        val latestFirst = compareByDescending<ProgressEntry> { it.questUser.endDate.orEmpty() }
            .thenByDescending { it.questUser.id }
        val firstCompletion = compareBy<ProgressEntry> { it.questUser.endDate ?: "9999-12-31" }
            .thenBy { it.questUser.id }
        val completedQuests = finished.groupBy { it.questId }.values
            .map { attempts ->
                val ordered = attempts.sortedWith(firstCompletion)
                ordered.first().copy(otherCompletions = ordered.drop(1).sortedWith(latestFirst).map { it.questUser })
            }
            .sortedWith(latestFirst)
        return Result.success(ProgressOverview(
            inProgress.sortedByDescending { it.questUser.id },
            family.sortedByDescending { it.questUser.id },
            completedQuests,
            completedQuests
        ))
    }
}
