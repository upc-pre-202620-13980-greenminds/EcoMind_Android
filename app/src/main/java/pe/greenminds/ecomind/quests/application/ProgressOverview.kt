package pe.greenminds.ecomind.quests.application

import pe.greenminds.ecomind.quests.domain.entity.ActivityUser
import pe.greenminds.ecomind.quests.domain.entity.QuestUser
import kotlin.math.roundToInt

// Read model for the overview; the assignment remains the source of quest progress.
data class ProgressEntry(
    val questUser: QuestUser,
    val title: String,
    val activityUsers: List<ActivityUser> = emptyList()
) {
    val questId: Long get() = questUser.questId
    val percent: Int get() = if (questUser.progress.isFinite()) {
        questUser.progress.coerceIn(0.0, 100.0).roundToInt()
    } else 0
}

data class ProgressOverview(
    val inProgress: List<ProgressEntry>,
    val familyQuests: List<ProgressEntry>,
    val finished: List<ProgressEntry>
)
