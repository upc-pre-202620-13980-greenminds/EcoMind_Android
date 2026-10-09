package pe.greenminds.ecomind.quests.infrastructure.implementation

import pe.greenminds.ecomind.quests.domain.entity.ActivityUser
import pe.greenminds.ecomind.quests.domain.entity.Quest
import pe.greenminds.ecomind.quests.domain.entity.QuestUser
import pe.greenminds.ecomind.quests.domain.repositories.QuestProgressRepository
import pe.greenminds.ecomind.quests.domain.valueobject.*
import javax.inject.Inject

// Isolated sample data for the restored progress view, not the remote quest catalog.
class LocalQuestProgressRepository @Inject constructor() : QuestProgressRepository {
    private val quests = listOf(
        quest(9, "Daily Quest", QuestType.DAILY_QUEST),
        quest(1, "Time to save up energy!", QuestType.COLLABORATIVE),
        quest(10, "Save up water", QuestType.FAMILY),
        quest(11, "Build a boat together!", QuestType.FAMILY),
        quest(12, "Save up water", QuestType.ACTIVITIES)
    )
    private val assignments = listOf(
        assignment(1, 9, 70.0), assignment(2, 1, 30.0),
        assignment(3, 10, 70.0), assignment(4, 11, 30.0),
        assignment(5, 12, 100.0, QuestStatus.COMPLETED)
    )
    private val activities = assignments.flatMap { assignment ->
        val first = (assignment.progress * 2).coerceAtMost(100.0)
        listOf(first, assignment.progress * 2 - first).mapIndexed { index, progress ->
            val id = assignment.id * 10 + index
            ActivityUser(
                id = id, questUserId = assignment.id, activityId = id,
                progress = progress,
                endDate = if (progress == 100.0) "2026-10-09" else null,
                description = "Activity ${index + 1}",
                configuration = emptyMap()
            )
        }
    }
    override suspend fun getQuestUsers() = Result.success(assignments)
    override suspend fun getActivityUsers() = Result.success(activities)
    override suspend fun getQuests() = Result.success(quests)

    private fun assignment(id: Long, questId: Long, progress: Double,
        status: QuestStatus = QuestStatus.IN_PROGRESS) = QuestUser(
        id = id, userId = 1, questId = questId, status = status, progress = progress,
        endDate = if (status == QuestStatus.COMPLETED) "2026-10-09" else null
    )
    private fun quest(id: Long, title: String, type: QuestType) = Quest(
        id = id, versionGroupId = id, versionNumber = 1,
        publicationStatus = QuestPublicationStatus.PUBLISHED, minigameId = null,
        title = title, description = title, category = QuestCategory.ENERGY,
        type = type, reward = QuestReward(gems = 10, ecopoints = 20),
        targetAge = null, estimatedMinutes = 10,
        theme = if (type == QuestType.COLLABORATIVE || type == QuestType.FAMILY)
            QuestTheme.COLLABORATIVE else QuestTheme.CHECKBOX,
        assignedDate = null, imageUrl = null
    )
}
