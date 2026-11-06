package pe.greenminds.ecomind.quests.infrastructure.remote

data class CollabQuestMemberDto(
    val id: Long,
    val sessionId: Long,
    val userId: Long,
    val ownerId: Long,
    val role: String,
    val status: String,
    val answerDate: String?,
    val revokeDate: String?
)
