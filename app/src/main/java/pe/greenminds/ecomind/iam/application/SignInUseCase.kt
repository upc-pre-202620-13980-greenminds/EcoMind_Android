package pe.greenminds.ecomind.iam.application

import pe.greenminds.ecomind.iam.domain.model.EmailAddress
import pe.greenminds.ecomind.iam.domain.model.Session
import pe.greenminds.ecomind.iam.domain.repositories.AuthRepository
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository
) {

    suspend operator fun invoke(email: String, password: String): Result<Session> {
        return authRepository
            .signIn(EmailAddress.normalize(email), password)
            .onSuccess { session ->
                // The stored session is what the loading screen reads on the next start
                sessionRepository.saveSession(session)
            }
    }
}
