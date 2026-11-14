package pe.greenminds.ecomind.quests.domain.entity

data class CollabQuestInvitation (
    val memberId: Long,
    val sessionId: Long,
    val questId: Long,
    val questTitle: String,
    val organizerUserId: Long,
    val invitedUserId: Long,
)
