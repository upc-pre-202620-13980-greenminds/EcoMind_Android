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
    val equippedCosmeticId: Long?
)

// GET /api/v1/friend?user_id={id}
data class FriendDto(
    val id: Long,
    val requesterId: Long,
    val receiverId: Long,
    val status: String
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
