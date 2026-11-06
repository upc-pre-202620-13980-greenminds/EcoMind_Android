package pe.greenminds.ecomind.iam.application

import pe.greenminds.ecomind.iam.domain.model.EmailAddress
import pe.greenminds.ecomind.iam.domain.repositories.AuthRepository
import javax.inject.Inject

class RequestPasswordRecoveryUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {

    suspend operator fun invoke(email: String): Result<Unit> {
        return authRepository.requestPasswordRecovery(EmailAddress.normalize(email))
    }
}
