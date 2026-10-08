package pe.greenminds.ecomind.iam.domain.services

// Rules a new password must meet: 8 to 72 characters with at least one letter and one digit
object PasswordPolicy {

    private const val MIN_LENGTH = 8
    private const val MAX_LENGTH = 72

    fun isSatisfiedBy(password: String): Boolean {
        if (password.length !in MIN_LENGTH..MAX_LENGTH) return false

        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        return hasLetter && hasDigit
    }
}
