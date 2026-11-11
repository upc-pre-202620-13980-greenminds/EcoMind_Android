package pe.greenminds.ecomind.users.application

import javax.inject.Inject

// The web services only return the code inside the profile of its owner
class GetMyInvitationCodeUseCase @Inject constructor(
    private val getCurrentProfile: GetCurrentProfileUseCase
) {

    suspend operator fun invoke(): Result<String> {
        val profile = getCurrentProfile().getOrElse { return Result.failure(it) }
        val code = profile.invitationCode
            ?: return Result.failure(IllegalStateException("The profile has no invitation code"))
        return Result.success(code)
    }
}
