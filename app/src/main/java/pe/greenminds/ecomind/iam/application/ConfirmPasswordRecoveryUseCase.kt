package pe.greenminds.ecomind.iam.application

import pe.greenminds.ecomind.iam.domain.repositories.AuthRepository
import javax.inject.Inject

class ConfirmPasswordRecoveryUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(token: String, newPassword: String): Result<Unit> {
        return authRepository.confirmPasswordRecovery(token.trim(), newPassword)
    }
}
