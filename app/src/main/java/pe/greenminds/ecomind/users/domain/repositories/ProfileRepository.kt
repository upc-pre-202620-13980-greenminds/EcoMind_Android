package pe.greenminds.ecomind.users.domain.repositories

import pe.greenminds.ecomind.users.domain.model.SocialRole
import pe.greenminds.ecomind.users.domain.model.UserProfile

interface ProfileRepository {

    suspend fun getProfile(userId: Long): Result<UserProfile>

    // The web services create the profile themselves when an account is verified
    suspend fun createProfile(userId: Long, name: String, socialRole: SocialRole)
}
