package pe.greenminds.ecomind.shared.infrastructure.remote

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import pe.greenminds.ecomind.shared.domain.model.SessionRequiredException
import pe.greenminds.ecomind.iam.domain.model.Session
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import retrofit2.HttpException
import javax.inject.Inject

// A request captures one identity. Never publish data returned after logout/account switching.
class RemoteAccess @Inject constructor(private val sessions: SessionRepository) {
    suspend fun <T> authenticated(block: suspend (Session, String) -> T): Result<T> {
        val session = sessions.getSession().first()
            ?: return Result.failure(SessionRequiredException())
        if (session.isExpired(System.currentTimeMillis()) || session.accessToken == "demo-access-token") {
            return Result.failure(SessionRequiredException())
        }
        return try {
            val value = block(session, "Bearer ${session.accessToken}")
            val current = sessions.getSession().first()
            if (current != session || current.isExpired(System.currentTimeMillis())) {
                Result.failure(SessionRequiredException())
            } else Result.success(value)
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            // Do not clear a newer session if an older request has just failed.
            if (e.code() == 401) {
                Result.failure(SessionRequiredException())
            } else Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
