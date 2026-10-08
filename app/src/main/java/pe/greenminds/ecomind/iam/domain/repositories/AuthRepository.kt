package pe.greenminds.ecomind.iam.domain.repositories

import pe.greenminds.ecomind.iam.domain.model.Session

interface AuthRepository {

    suspend fun signIn(email: String, password: String): Result<Session>
}
