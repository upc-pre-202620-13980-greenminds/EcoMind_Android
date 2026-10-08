package pe.greenminds.ecomind.iam.domain.model

object PersonName {

    private const val MIN_LENGTH = 2
    private const val MAX_LENGTH = 120

    fun isValid(value: String): Boolean = value.trim().length in MIN_LENGTH..MAX_LENGTH
}
