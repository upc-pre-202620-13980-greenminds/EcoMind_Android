package pe.greenminds.ecomind.iam.infrastructure.remote

data class SignInRequest(
    val email: String,
    val password: String
)

data class SignInResponse(
    val accessToken: String,
    val expiresAt: String,
    val accountId: Long,
    val email: String
)

data class SignUpRequest(
    val name: String,
    val email: String,
    val password: String,
    val socialRole: String
)

data class SignUpResponse(
    val email: String,
    val expiresAt: String
)

data class VerifyEmailRequest(
    val email: String,
    val code: String
)

data class VerifyEmailResponse(
    val accountId: Long,
    val email: String
)