package pe.greenminds.ecomind.users.domain.repositories

import pe.greenminds.ecomind.users.domain.model.UserProfile

interface ProfileRepository {

    suspend fun getProfile(userId: Long): Result<UserProfile>
}
