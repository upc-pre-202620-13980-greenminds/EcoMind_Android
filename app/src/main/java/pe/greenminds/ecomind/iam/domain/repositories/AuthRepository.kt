package pe.greenminds.ecomind.iam.domain.repositories

import pe.greenminds.ecomind.iam.domain.model.PendingRegistration
import pe.greenminds.ecomind.iam.domain.model.Session
import pe.greenminds.ecomind.iam.domain.model.SocialRole

interface AuthRepository {

    suspend fun signIn(email: String, password: String): Result<Session>

    // Starts a registration; sending it again for the same email replaces the previous code
    suspend fun signUp(
        name: String,
        email: String,
        password: String,
        role: SocialRole
    ): Result<PendingRegistration>

    // Creates the account when the code is correct
    suspend fun verifyEmail(email: String, code: String): Result<Unit>

    suspend fun requestPasswordRecovery(email: String): Result<Unit>

    suspend fun confirmPasswordRecovery(token: String, newPassword: String): Result<Unit>

    suspend fun logout(): Result<Unit>
}
