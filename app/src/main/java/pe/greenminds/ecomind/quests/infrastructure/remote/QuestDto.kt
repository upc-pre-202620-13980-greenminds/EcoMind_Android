package pe.greenminds.ecomind.quests.infrastructure.remote

// Same fields, names and types as QuestResource of the web services.
// GET /api/v1/quests, GET /api/v1/quests/{questId} and GET /api/v1/quests/search
data class QuestDto(
    val id: Long,
    val versionGroupId: Long,
    val versionNumber: Int,
    val publicationStatus: String,
    val minigameId: Long?,
    val title: String,
    val description: String,
    val category: String,
    val type: String,
    val gemReward: Int,
    val ecopoints: Int,
    val age: Int?,
    val time: Int?,
    val theme: String,
    val assignedDate: String?,
    val image: String?
)
