package pe.greenminds.ecomind.users.infrastructure.remote

// Same fields, names and types as the resources of the web services (Users context)

// GET /api/v1/user/{id}
data class UserProfileDto(
    val id: Long,
    val name: String,
    val socialRole: String,
    val streak: Int,
    val lastStreakDate: String?,
    val ecopoints: Int,
    val gemBalance: Int,
    val equippedCosmeticId: Long?,
    // Only filled in when the profile is the one of the user of the token
    val invitationCode: String? = null
)

// PUT /api/v1/user/{id}: every field is required except lastStreakDate
data class UpdateUserProfileRequest(
    val name: String,
    val streak: Int,
    val lastStreakDate: String?,
    val ecopoints: Int,
    val gemBalance: Int
)

// GET /api/v1/friend?user_id={id}
data class FriendDto(
    val id: Long,
    val requesterId: Long,
    val receiverId: Long,
    val status: String,
    // ISO-8601 date; a pending request expires 48 hours after it
    val createdAt: String? = null
)

// POST /api/v1/friend: exactly one of the two is sent. Gson leaves the null one out of the body.
data class SendFriendRequestRequest(
    val receiverId: Long? = null,
    val invitationCode: String? = null
)

// PUT /api/v1/friend/{id}: true accepts the request, false rejects it
data class RespondFriendRequestRequest(
    val accepted: Boolean
)

// GET /api/v1/family
data class FamilyDto(
    val id: Long,
    val name: String,
    val commitment: String,
    val members: List<FamilyMemberDto>
)

// GET /api/v1/family_user?user_id={id}
data class FamilyMemberDto(
    val id: Long,
    val familyId: Long,
    val userId: Long,
    val familyRole: String
)
