package pe.greenminds.ecomind.iam.application

import pe.greenminds.ecomind.iam.domain.model.EmailAddress
import pe.greenminds.ecomind.iam.domain.repositories.AuthRepository
import javax.inject.Inject

class VerifyEmailUseCase @Inject constructor(private val repository: AuthRepository) {

    suspend operator fun invoke(email: String, code: String): Result<Unit> {
        return repository.verifyEmail(EmailAddress.normalize(email), code)
    }
}
