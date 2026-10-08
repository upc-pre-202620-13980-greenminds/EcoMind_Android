package pe.greenminds.ecomind.iam.domain.model

// Registration waiting for the email to be verified with the code
data class PendingRegistration(
    val email: String,
    val expiresAtMillis: Long
)
