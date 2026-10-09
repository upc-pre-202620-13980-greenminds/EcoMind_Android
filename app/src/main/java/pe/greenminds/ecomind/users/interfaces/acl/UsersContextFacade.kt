package pe.greenminds.ecomind.users.interfaces.acl

import pe.greenminds.ecomind.users.domain.model.SocialRole
import pe.greenminds.ecomind.users.domain.repositories.FamilyRepository
import pe.greenminds.ecomind.users.domain.repositories.ProfileRepository
import javax.inject.Inject

// Entry point other bounded contexts use, so they do not depend on the internals of Users
class UsersContextFacade @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val familyRepository: FamilyRepository
) {

    // Called by IAM when an account is created; the role travels as text between contexts
    suspend fun createProfile(userId: Long, name: String, socialRole: String) {
        profileRepository.createProfile(userId, name, SocialRole.valueOf(socialRole))
    }

    // Used by Gamification to know which family of the ranking is the one of the user
    suspend fun getFamilyIdOf(userId: Long): Long? {
        return familyRepository.getFamilyOf(userId).getOrNull()?.id
    }

    suspend fun spendGems(userId: Long, amount: Int): Result<Int> {
        return profileRepository.spendGems(userId, amount)
    }
}
