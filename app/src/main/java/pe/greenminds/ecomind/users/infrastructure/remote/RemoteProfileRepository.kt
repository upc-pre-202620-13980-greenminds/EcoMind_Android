package pe.greenminds.ecomind.users.infrastructure.remote

import pe.greenminds.ecomind.shared.infrastructure.remote.RemoteAccess
import pe.greenminds.ecomind.users.domain.model.SocialRole
import pe.greenminds.ecomind.users.domain.repositories.ProfileRepository
import javax.inject.Inject

class RemoteProfileRepository @Inject constructor(private val api: UsersApi, private val access: RemoteAccess) : ProfileRepository {
    override suspend fun getProfile(userId: Long) = access.authenticated { _, token ->
        api.profile(token, userId).also { check(it.id == userId) }.toDomain()
    }
    override suspend fun createProfile(userId: Long, name: String, socialRole: SocialRole) {
        error("The service creates the profile when registration is verified")
    }
}
