package pe.greenminds.ecomind.quests.domain.model

enum class QuestStatus {
    READY_TO_COMPLETE,
    IN_PROGRESS,
    COMPLETED,
    EXPIRED
}

// A quest assigned to a user; progress goes from 0 to 100
data class QuestUser(
    val id: Long,
    val questId: Long,
    val status: QuestStatus,
    val progress: Double
)

enum class FamilyPlanStatus {
    DRAFT,
    ACTIVE,
    COMPLETED,
    CANCELLED
}

data class FamilyPlanItem(
    val id: Long,
    val questId: Long,
    val progress: Double
)

// Quests a family chose to do together
data class FamilyPlan(
    val id: Long,
    val familyId: Long,
    val status: FamilyPlanStatus,
    val progress: Double,
    val items: List<FamilyPlanItem>
)

// One row of the progress screen: the title of a quest and how much of it is done
data class ProgressEntry(
    val questId: Long,
    val title: String,
    val percent: Int
)

data class ProgressOverview(
    val inProgress: List<ProgressEntry>,
    val familyQuests: List<ProgressEntry>,
    val finished: List<ProgressEntry>
)
