package pe.greenminds.ecomind.monetization.domain.model

data class Multiplier(
    val id: String,
    val name: String,
    val description: String,
    val factor: Double,
    val durationMinutes: Int,
    val priceInGems: Int,
    val imageReference: String
)

data class StreakProtector(
    val id: String,
    val name: String,
    val description: String,
    val priceInGems: Int,
    val imageReference: String
)
