package pe.greenminds.ecomind.iam.domain.repositories

import kotlinx.coroutines.flow.Flow
import pe.greenminds.ecomind.iam.domain.model.Session

interface SessionRepository {

    fun getSession(): Flow<Session?>

    suspend fun saveSession(session: Session)

    suspend fun clearSession()
}
