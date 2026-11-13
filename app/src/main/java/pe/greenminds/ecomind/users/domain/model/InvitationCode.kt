package pe.greenminds.ecomind.users.domain.model

// Same format as the web services: 8 capital letters or digits, without 0, O, 1 and I
object InvitationCode {

    private const val GROUP_SIZE = 4
    private val FORMAT = Regex("^[A-HJ-NP-Z2-9]{8}$")

    // The code is shown and copied with a space ("K7MP 4QXZ"), so spaces and lower case are removed
    fun normalize(input: String): String = input.filterNot { it.isWhitespace() }.uppercase()

    fun isValid(code: String): Boolean = FORMAT.matches(code)

    fun display(code: String): String = code.chunked(GROUP_SIZE).joinToString(" ")
}
