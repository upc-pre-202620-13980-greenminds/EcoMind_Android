package pe.greenminds.ecomind.iam.domain.model

object VerificationCode {

    const val LENGTH = 6

    fun isValid(value: String): Boolean {
        return value.length == LENGTH && value.all { it.isDigit() }
    }
}
