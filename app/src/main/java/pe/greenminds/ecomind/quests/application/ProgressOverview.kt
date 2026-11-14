package pe.greenminds.ecomind.quests.application

import pe.greenminds.ecomind.quests.domain.entity.ActivityUser
import pe.greenminds.ecomind.quests.domain.entity.QuestUser
import pe.greenminds.ecomind.quests.domain.valueobject.QuestType
import kotlin.math.roundToInt

// Read model for the overview; the assignment remains the source of quest progress.
data class ProgressEntry(
    val questUser: QuestUser,
    val title: String,
    val activityUsers: List<ActivityUser> = emptyList(),
    val questType: QuestType? = null,
    val otherCompletions: List<QuestUser> = emptyList()
) {
    val questId: Long get() = questUser.questId
    val percent: Int get() = if (questUser.progress.isFinite()) {
        questUser.progress.coerceIn(0.0, 100.0).roundToInt()
    } else 0
}

data class ProgressOverview(
    val inProgress: List<ProgressEntry>,
    val familyQuests: List<ProgressEntry>,
    val finished: List<ProgressEntry>,
    val history: List<ProgressEntry> = finished
)

enum class ProgressSection { IN_PROGRESS, FAMILY, HISTORY }

internal fun groupQuestCompletions(entries: List<ProgressEntry>): List<ProgressEntry> {
    val latestFirst = compareByDescending<ProgressEntry> { it.questUser.endDate.orEmpty() }
        .thenByDescending { it.questUser.id }
    val firstCompletion = compareBy<ProgressEntry> { it.questUser.endDate ?: "9999-12-31" }
        .thenBy { it.questUser.id }
    return entries.groupBy { it.questId }.values.map { attempts ->
        val ordered = attempts.sortedWith(firstCompletion)
        ordered.first().copy(otherCompletions = ordered.drop(1).sortedWith(latestFirst).map { it.questUser })
    }.sortedWith(latestFirst)
}
