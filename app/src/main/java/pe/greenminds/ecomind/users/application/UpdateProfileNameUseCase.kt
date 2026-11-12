package pe.greenminds.ecomind.users.application

import kotlinx.coroutines.flow.first
import pe.greenminds.ecomind.iam.domain.model.PersonName
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.users.domain.model.UserProfile
import pe.greenminds.ecomind.users.domain.repositories.ProfileRepository
import javax.inject.Inject

class UpdateProfileNameUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val profileRepository: ProfileRepository
) {

    suspend operator fun invoke(name: String): Result<UserProfile> {
        // Same rule as the registration: 2 to 120 characters, without surrounding spaces
        val trimmed = name.trim()
        if (!PersonName.isValid(trimmed)) {
            return Result.failure(IllegalArgumentException("The name must have 2 to 120 characters"))
        }

        val session = sessionRepository.getSession().first()
            ?: return Result.failure(IllegalStateException("There is no stored session"))

        // Only the owner can edit a profile, so the id always comes from the session
        return profileRepository.updateName(session.accountId, trimmed)
    }
}
