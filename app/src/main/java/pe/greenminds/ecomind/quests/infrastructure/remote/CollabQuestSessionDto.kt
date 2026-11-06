package pe.greenminds.ecomind.quests.infrastructure.remote

data class CollabQuestSessionDto(
    val id: Long,
    val questId: Long,
    val ownerUserId: Long,
    val status: String,
    val createdAt: String?,
    val startedAt: String?,
    val completedAt: String?
)
