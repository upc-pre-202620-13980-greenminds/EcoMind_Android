package pe.greenminds.ecomind.iam.infrastructure.implementation

import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import pe.greenminds.ecomind.iam.domain.model.AuthError
import pe.greenminds.ecomind.iam.domain.model.AuthException
import pe.greenminds.ecomind.iam.domain.model.PendingRegistration
import pe.greenminds.ecomind.iam.domain.model.Session
import pe.greenminds.ecomind.iam.domain.model.SocialRole
import pe.greenminds.ecomind.iam.domain.repositories.AuthRepository
import pe.greenminds.ecomind.iam.infrastructure.mapper.toDomain
import pe.greenminds.ecomind.iam.infrastructure.remote.AuthApi
import pe.greenminds.ecomind.iam.infrastructure.remote.ConfirmPasswordRecoveryRequest
import pe.greenminds.ecomind.iam.infrastructure.remote.PasswordRecoveryRequest
import pe.greenminds.ecomind.iam.infrastructure.remote.SignInRequest
import pe.greenminds.ecomind.iam.infrastructure.remote.SignUpRequest
import pe.greenminds.ecomind.iam.infrastructure.remote.VerifyEmailRequest
import pe.greenminds.ecomind.shared.infrastructure.remote.ErrorDto
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject

class RemoteAuthRepository @Inject constructor(
    private val api: AuthApi
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): Result<Session> {
        return execute(
            request = { api.signIn(SignInRequest(email, password)) },
            transform = { it.toDomain() }
        )
    }

    override suspend fun signUp(
        name: String,
        email: String,
        password: String,
        role: SocialRole
    ): Result<PendingRegistration> {
        return execute(
            request = { api.signUp(SignUpRequest(name, email, password, role.name)) },
            transform = { it.toDomain() }
        )
    }

    override suspend fun verifyEmail(email: String, code: String): Result<Unit> {
        return execute(
            request = { api.verifyEmail(VerifyEmailRequest(email, code)) },
            transform = { }
        )
    }

    override suspend fun requestPasswordRecovery(email: String): Result<Unit> {
        return execute(
            request = { api.requestPasswordRecovery(PasswordRecoveryRequest(email)) },
            transform = { }
        )
    }

    override suspend fun confirmPasswordRecovery(token: String, newPassword: String): Result<Unit> {
        return executeEmpty { api.confirmPasswordRecovery(ConfirmPasswordRecoveryRequest(token, newPassword)) }
    }

    override suspend fun logout(): Result<Unit> {
        return executeEmpty { api.logout() }
    }

    private suspend fun executeEmpty(request: suspend () -> Response<Unit>): Result<Unit> {
        return try {
            val response = request()
            if (response.isSuccessful) Result.success(Unit) else Result.failure(toException(response))
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    // Shares response handling while keeping DTOs inside infrastructure.
    private suspend fun <Dto : Any, Model> execute(
        request: suspend () -> Response<Dto>,
        transform: (Dto) -> Model
    ): Result<Model> {
        return try {
            val response = request()
            if (!response.isSuccessful) {
                Result.failure(toException(response))
            } else {
                val body = response.body()
                    ?: return Result.failure(IllegalStateException("Empty authentication response"))
                Result.success(transform(body))
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    private fun toException(response: Response<*>): Exception {
        val error = try {
            response.errorBody()?.use { body ->
                Gson().fromJson(body.string(), ErrorDto::class.java)
            }
        } catch (_: Exception) {
            null
        }

        return when (error?.code) {
            "INVALID_CREDENTIALS" -> AuthException(AuthError.INVALID_CREDENTIALS)
            "EMAIL_CONFLICT" -> AuthException(AuthError.EMAIL_CONFLICT)
            "VERIFICATION_CODE_INVALID" -> AuthException(AuthError.VERIFICATION_CODE_INVALID)
            "PASSWORD_POLICY_VIOLATION" -> AuthException(AuthError.PASSWORD_POLICY_VIOLATION)
            "PASSWORD_RESET_TOKEN_INVALID" -> AuthException(AuthError.PASSWORD_RESET_TOKEN_INVALID)
            else -> HttpException(response)
        }
    }
}
