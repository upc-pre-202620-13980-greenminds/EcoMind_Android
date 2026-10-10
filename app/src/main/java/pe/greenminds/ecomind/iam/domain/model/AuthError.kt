package pe.greenminds.ecomind.iam.domain.model

enum class AuthError {
    INVALID_CREDENTIALS,
    EMAIL_CONFLICT,
    VERIFICATION_CODE_INVALID,
    UNKNOWN
}

// Carries the reason of a failed operation inside a Result
class AuthException(val error: AuthError) : Exception(error.name)
