package pe.greenminds.ecomind.quests.domain.model

enum class QuestCategory {
    WATER,
    RECYCLE,
    ENERGY
}

enum class QuestType {
    COLLABORATIVE,
    MINIGAME,
    ACTIVITIES,
    DAILY_QUEST,
    FAMILY
}

enum class QuestTheme {
    CHECKBOX,
    MINIGAME,
    COLLABORATIVE
}

data class Quest(
    val id: Long,
    val title: String,
    val description: String,
    val category: QuestCategory,
    val type: QuestType,
    val theme: QuestTheme,
    val gemReward: Int,
    val ecopoints: Int,
    // Expected time to complete the quest, in minutes
    val minutes: Int,
    val recommendedAge: Int,
    val imageUrl: String?,
    val minigameId: Long?
)

// Values the user can search by; null means "any"
data class QuestFilter(
    val title: String = "",
    val category: QuestCategory? = null,
    val questType: QuestType? = null
)
