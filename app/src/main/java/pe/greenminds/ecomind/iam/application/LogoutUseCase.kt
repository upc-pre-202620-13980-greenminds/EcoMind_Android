package pe.greenminds.ecomind.iam.application

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import pe.greenminds.ecomind.iam.domain.repositories.AuthRepository
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val sessionRepository: SessionRepository
) {

    suspend operator fun invoke(): Result<Unit> {
        return try {
            authRepository.logout()
        } finally {
            // Ending a local session must not depend on the server or screen lifetime.
            withContext(NonCancellable) {
                sessionRepository.clearSession()
            }
        }
    }
}
