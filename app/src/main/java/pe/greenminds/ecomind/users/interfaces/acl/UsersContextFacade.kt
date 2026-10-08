package pe.greenminds.ecomind.users.interfaces.acl

import pe.greenminds.ecomind.users.domain.model.SocialRole
import pe.greenminds.ecomind.users.domain.repositories.ProfileRepository
import javax.inject.Inject

// Entry point other bounded contexts use, so they do not depend on the internals of Users
class UsersContextFacade @Inject constructor(
    private val profileRepository: ProfileRepository
) {

    // Called by IAM when an account is created; the role travels as text between contexts
    suspend fun createProfile(userId: Long, name: String, socialRole: String) {
        profileRepository.createProfile(userId, name, SocialRole.valueOf(socialRole))
    }
}
