package pe.greenminds.ecomind.iam.application

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import javax.inject.Inject

class ClearSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {

    suspend operator fun invoke(): Boolean = withContext(NonCancellable) {
        try {
            sessionRepository.clearSession()
            true
        } catch (_: Exception) {
            false
        }
    }
}
