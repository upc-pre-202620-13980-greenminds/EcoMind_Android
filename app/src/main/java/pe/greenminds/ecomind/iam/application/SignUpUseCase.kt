package pe.greenminds.ecomind.iam.application

import pe.greenminds.ecomind.iam.domain.model.EmailAddress
import pe.greenminds.ecomind.iam.domain.model.PendingRegistration
import pe.greenminds.ecomind.iam.domain.model.SocialRole
import pe.greenminds.ecomind.iam.domain.repositories.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(private val repository: AuthRepository) {

    suspend operator fun invoke(
        name: String,
        email: String,
        password: String,
        role: SocialRole
    ): Result<PendingRegistration> {
        return repository.signUp(name.trim(), EmailAddress.normalize(email), password, role)
    }
}
