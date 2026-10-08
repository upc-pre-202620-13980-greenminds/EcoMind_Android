package pe.greenminds.ecomind.iam.domain.model

enum class AuthError {
    INVALID_CREDENTIALS,
    UNKNOWN
}

// Carries the reason of a failed operation inside a Result
class AuthException(val error: AuthError) : Exception(error.name)
