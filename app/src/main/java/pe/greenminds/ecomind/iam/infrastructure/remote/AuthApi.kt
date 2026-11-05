package pe.greenminds.ecomind.iam.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("authentication/sign-in")
    suspend fun signIn(
        @Body request: SignInRequest
    ): Response<SignInResponse>

    @POST("authentication/sign-up")
    suspend fun signUp(
        @Body request: SignUpRequest
    ): Response<SignUpResponse>

    @POST("authentication/verify-email")
    suspend fun verifyEmail(
        @Body request: VerifyEmailRequest
    ): Response<VerifyEmailResponse>

    @POST("authentication/password-recovery/request")
    suspend fun requestPasswordRecovery(
        @Body request: PasswordRecoveryRequest
    ): Response<PasswordRecoveryResponse>

    @POST("authentication/password-recovery/confirm")
    suspend fun confirmPasswordRecovery(
        @Body request: ConfirmPasswordRecoveryRequest
    ): Response<Unit>

    @POST("authentication/logout")
    suspend fun logout(): Response<Unit>
}
