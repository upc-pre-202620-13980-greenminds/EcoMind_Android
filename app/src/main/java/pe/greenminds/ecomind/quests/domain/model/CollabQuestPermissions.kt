package pe.greenminds.ecomind.quests.domain.model

data class CollabQuestPermissions(
    val canInvite: Boolean,
    val canStart: Boolean,
    val canAcceptInvitation: Boolean,
    val canLeave: Boolean,
    val canRemoveMembers: Boolean,
    val canDeleteSession: Boolean
)
