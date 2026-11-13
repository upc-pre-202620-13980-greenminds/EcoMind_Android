package pe.greenminds.ecomind.users.domain.model

data class UserProfile(
    // Same value as the id of the account
    val id: Long,
    val name: String,
    val socialRole: SocialRole,
    val streak: Int,
    val ecopoints: Int,
    val gemBalance: Int,
    // Stored apart from the profile: the web services have no personal commitment
    val commitment: String? = null,
    // Only known for the profile of the current user
    val invitationCode: String? = null,
    // Image of the equipped avatar; null when the user has none or the route does not send it
    val avatarImageReference: String? = null
)
