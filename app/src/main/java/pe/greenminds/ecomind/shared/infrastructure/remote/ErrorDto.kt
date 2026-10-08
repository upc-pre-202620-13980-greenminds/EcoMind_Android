package pe.greenminds.ecomind.shared.infrastructure.remote

// Body the web services return when a request fails: {code, message, details}
data class ErrorDto(
    val code: String,
    val message: String,
    val details: String? = null
)

// Carries the error of the web services inside a Result
class ApiException(val code: String, message: String) : Exception(message)

fun ErrorDto.toException(): ApiException = ApiException(code, message)
