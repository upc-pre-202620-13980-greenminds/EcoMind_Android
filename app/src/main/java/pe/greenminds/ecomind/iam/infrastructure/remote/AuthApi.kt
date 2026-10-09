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
}