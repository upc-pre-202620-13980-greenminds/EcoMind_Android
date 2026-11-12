package pe.greenminds.ecomind.community.domain.entity

data class EventParticipation(
    val event: CommunityEvent,
    val communityName: String,
    val creatorName: String,
    val participantCount: Int,
    val totalCapacity: Int,
    val registrationId: Long?,
    val joined: Boolean,
    val canCancelGroup: Boolean,
    val creator: Boolean,
    val chatExpiresAt: String,
    val chatAvailable: Boolean,
    val familyId: Long?
)