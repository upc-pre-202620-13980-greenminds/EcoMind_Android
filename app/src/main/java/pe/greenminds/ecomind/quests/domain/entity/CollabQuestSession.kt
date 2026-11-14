package pe.greenminds.ecomind.quests.domain.entity

data class CollabQuestSession(
    val id: Long,
    val questId: Long,
    val ownerUserId: Long,
    val status: String,
    val createdAt: String?,
    val startedAt: String?,
    val completedAt: String?
)
