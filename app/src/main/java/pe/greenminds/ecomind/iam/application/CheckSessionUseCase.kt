package pe.greenminds.ecomind.iam.application

import kotlinx.coroutines.flow.first
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import javax.inject.Inject

class CheckSessionUseCase @Inject constructor(private val repository: SessionRepository) {

    // True only when there is a stored session whose token is still valid
    suspend operator fun invoke(): Boolean {
        val session = repository.getSession().first() ?: return false
        if (session.isExpired(System.currentTimeMillis()) ||
            (session.accessToken == "demo-access-token") == pe.greenminds.ecomind.BuildConfig.REMOTE_BACKEND) {
            repository.clearSession()
            return false
        }
        return true
    }
}
