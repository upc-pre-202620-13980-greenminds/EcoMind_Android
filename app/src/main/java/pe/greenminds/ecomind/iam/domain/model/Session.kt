package pe.greenminds.ecomind.iam.domain.model

data class Session(
    val accountId: Long,
    val email: String,
    val accessToken: String,
    // Epoch milliseconds: java.time needs API 26 and the app supports API 24
    val expiresAtMillis: Long
) {
    fun isExpired(nowMillis: Long): Boolean = nowMillis >= expiresAtMillis
}
