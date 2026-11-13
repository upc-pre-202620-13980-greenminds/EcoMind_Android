package pe.greenminds.ecomind.users.application

import kotlinx.coroutines.flow.first
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.users.domain.model.UserProfile
import pe.greenminds.ecomind.users.domain.repositories.CommitmentRepository
import pe.greenminds.ecomind.users.domain.repositories.ProfileRepository
import javax.inject.Inject

// Only dependency of this context on IAM: the id of the user comes from the stored session
class GetCurrentProfileUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val profileRepository: ProfileRepository,
    private val commitmentRepository: CommitmentRepository
) {

    suspend operator fun invoke(): Result<UserProfile> {
        val session = sessionRepository.getSession().first()
            ?: return Result.failure(IllegalStateException("There is no stored session"))

        // The commitment is stored apart from the profile, so both are joined here.
        // If it cannot be read, the profile is still shown without it.
        return profileRepository.getProfile(session.accountId).map { profile ->
            val commitment = commitmentRepository.getCommitment(session.accountId).getOrNull()
            profile.copy(commitment = commitment)
        }
    }
}
