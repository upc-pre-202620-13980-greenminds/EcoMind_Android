package pe.greenminds.ecomind.iam.domain.model

object EmailAddress {

    // Same limit the web services apply to the email of an account
    private const val MAX_LENGTH = 255

    // text@domain.extension, without spaces
    private val PATTERN = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+$")

    fun isValid(value: String): Boolean {
        return value.length <= MAX_LENGTH && PATTERN.matches(value)
    }

    // Accounts are identified by the email in lowercase and without surrounding spaces
    fun normalize(value: String): String = value.trim().lowercase()
}
