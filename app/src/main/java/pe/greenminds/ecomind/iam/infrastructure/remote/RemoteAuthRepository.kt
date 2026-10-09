package pe.greenminds.ecomind.iam.infrastructure.remote

import kotlinx.coroutines.CancellationException
import pe.greenminds.ecomind.gamification.infrastructure.remote.instantToMillis
import pe.greenminds.ecomind.iam.domain.model.*
import pe.greenminds.ecomind.iam.domain.repositories.AuthRepository
import retrofit2.HttpException
import retrofit2.http.Body
import retrofit2.http.POST
import javax.inject.Inject

data class SignInBody(val email: String, val password: String)
data class RegistrationBody(val name: String, val email: String, val password: String, val socialRole: String)
data class VerificationBody(val email: String, val code: String)
data class AuthenticationDto(val accessToken: String, val expiresAt: String, val accountId: Long, val email: String)
data class PendingRegistrationDto(val email: String, val expiresAt: String)
interface AuthenticationApi {
    @POST("authentication/sign-in") suspend fun signIn(@Body body: SignInBody): AuthenticationDto
    @POST("authentication/sign-up") suspend fun signUp(@Body body: RegistrationBody): PendingRegistrationDto
    @POST("authentication/verify-email") suspend fun verify(@Body body: VerificationBody): retrofit2.Response<Unit>
}
class RemoteAuthRepository @Inject constructor(private val api: AuthenticationApi) : AuthRepository {
    override suspend fun signIn(email: String, password: String): Result<Session> = request {
        val dto = api.signIn(SignInBody(email, password))
        Session(dto.accountId, dto.email, dto.accessToken, instantToMillis(dto.expiresAt))
    }
    override suspend fun signUp(name: String, email: String, password: String, role: SocialRole): Result<PendingRegistration> = request {
        api.signUp(RegistrationBody(name, email, password, role.name)).let {
            PendingRegistration(it.email, instantToMillis(it.expiresAt))
        }
    }
    override suspend fun verifyEmail(email: String, code: String): Result<Unit> = request {
        val response = api.verify(VerificationBody(email, code))
        if (!response.isSuccessful) throw HttpException(response)
    }
    private suspend fun <T> request(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) { throw e
    } catch (e: HttpException) {
        Result.failure(AuthException(when (e.code()) {
            401 -> AuthError.INVALID_CREDENTIALS
            409 -> AuthError.EMAIL_CONFLICT
            422 -> AuthError.VERIFICATION_CODE_INVALID
            else -> AuthError.UNKNOWN
        }))
    } catch (e: Exception) { Result.failure(e) }
}
