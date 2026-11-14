package pe.greenminds.ecomind.quests.domain.entity

data class CollabQuestMember(
    val id: Long,
    val sessionId: Long,
    val userId: Long,
    val ownerId: Long,
    val role: String,
    val status: String,
    val answerDate: String?,
    val revokeDate: String?
)
