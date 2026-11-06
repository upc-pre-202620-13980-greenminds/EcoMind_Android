package pe.greenminds.ecomind.quests.infrastructure.remote

data class CollabQuestPermissionsDto(
    val canInvite: Boolean,
    val canStart: Boolean,
    val canAcceptInvitation: Boolean,
    val canLeave: Boolean,
    val canRemoveMembers: Boolean,
    val canDeleteSession: Boolean
)
