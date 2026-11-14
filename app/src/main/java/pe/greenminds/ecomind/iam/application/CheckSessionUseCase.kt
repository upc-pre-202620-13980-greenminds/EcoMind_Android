package pe.greenminds.ecomind.iam.application

import kotlinx.coroutines.flow.first
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import javax.inject.Inject

class CheckSessionUseCase @Inject constructor(private val repository: SessionRepository) {

    // A saved account can open local data. The interceptor still requires a valid token.
    suspend operator fun invoke(): Boolean = repository.getSession().first() != null
}
