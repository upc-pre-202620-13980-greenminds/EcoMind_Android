package pe.greenminds.ecomind.quests.infrastructure.remote

data class CollabQuestInvitationDto(
    val memberId: Long,
    val sessionId: Long,
    val questId: Long,
    val questTitle: String,
    val organizerUserId: Long,
    val invitedUserId: Long,
    val memberStatus: String,
    val sessionStatus: String
)