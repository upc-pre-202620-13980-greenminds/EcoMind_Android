package pe.greenminds.ecomind.users.domain.repositories

import pe.greenminds.ecomind.users.domain.model.SocialRole
import pe.greenminds.ecomind.users.domain.model.UserProfile

interface ProfileRepository {

    suspend fun getProfile(userId: Long): Result<UserProfile>

    // Every registered profile; the search of friends filters it by name
    suspend fun getProfiles(): Result<List<UserProfile>>

    // The web services create the profile themselves when an account is verified
    suspend fun createProfile(userId: Long, name: String, socialRole: SocialRole)

    suspend fun spendGems(userId: Long, amount: Int): Result<Int>

    // Replaces the name shown in the profile; the rest of the profile keeps its values
    suspend fun updateName(userId: Long, name: String): Result<UserProfile>
}
