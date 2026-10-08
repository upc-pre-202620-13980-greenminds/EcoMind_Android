package pe.greenminds.ecomind.quests.application

import kotlinx.coroutines.flow.first
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.quests.domain.model.ProgressEntry
import pe.greenminds.ecomind.quests.domain.model.ProgressOverview
import pe.greenminds.ecomind.quests.domain.model.QuestStatus
import pe.greenminds.ecomind.quests.domain.repositories.QuestProgressRepository
import pe.greenminds.ecomind.quests.domain.repositories.QuestRepository
import javax.inject.Inject
import kotlin.math.roundToInt

class GetProgressOverviewUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val progressRepository: QuestProgressRepository,
    private val questRepository: QuestRepository
) {

    suspend operator fun invoke(): Result<ProgressOverview> {
        val session = sessionRepository.getSession().first()
            ?: return Result.failure(IllegalStateException("There is no stored session"))
        val userId = session.accountId

        val inProgress = progressRepository.getQuestUsers(userId, QuestStatus.IN_PROGRESS)
            .getOrElse { return Result.failure(it) }
        val finished = progressRepository.getQuestUsers(userId, QuestStatus.COMPLETED)
            .getOrElse { return Result.failure(it) }
        val familyPlan = progressRepository.getActiveFamilyPlan(userId)
            .getOrElse { return Result.failure(it) }

        val overview = ProgressOverview(
            inProgress = inProgress.mapNotNull { entryOf(it.questId, it.progress) },
            familyQuests = familyPlan?.items?.mapNotNull { entryOf(it.questId, it.progress) }
                ?: emptyList(),
            finished = finished.mapNotNull { entryOf(it.questId, it.progress) }
        )
        return Result.success(overview)
    }

    // The progress only has the id of the quest, so the title comes from the quest itself.
    // A quest that cannot be read is left out instead of failing the whole screen.
    private suspend fun entryOf(questId: Long, progress: Double): ProgressEntry? {
        val quest = questRepository.getQuest(questId).getOrNull() ?: return null
        return ProgressEntry(
            questId = quest.id,
            title = quest.title,
            percent = progress.roundToInt().coerceIn(0, 100)
        )
    }
}
