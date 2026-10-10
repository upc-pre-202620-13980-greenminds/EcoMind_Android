package pe.greenminds.ecomind.users.domain.model

data class UserProfile(
    // Same value as the id of the account
    val id: Long,
    val name: String,
    val socialRole: SocialRole,
    val streak: Int,
    val ecopoints: Int,
    val gemBalance: Int,
    // Provisional: the web services do not return a personal commitment yet
    val commitment: String? = null
)
