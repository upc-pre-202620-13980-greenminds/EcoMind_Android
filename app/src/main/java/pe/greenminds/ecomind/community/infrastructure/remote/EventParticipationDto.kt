package pe.greenminds.ecomind.community.infrastructure.remote

import com.google.gson.annotations.SerializedName

data class EventParticipationDto(
    val event: CommunityEventDto,
    @SerializedName("community_name") val communityName: String,
    @SerializedName("creator_name") val creatorName: String,
    @SerializedName("participant_count") val participantCount: Int,
    @SerializedName("total_capacity") val totalCapacity: Int,
    @SerializedName("registration_id") val registrationId: Long?,
    val joined: Boolean,
    @SerializedName("can_cancel_group") val canCancelGroup: Boolean,
    val creator: Boolean,
    @SerializedName("chat_expires_at") val chatExpiresAt: String,
    @SerializedName("chat_available") val chatAvailable: Boolean,
    @SerializedName("family_id") val familyId: Long?
)