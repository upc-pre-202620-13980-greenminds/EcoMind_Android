package pe.greenminds.ecomind.monetization.domain.model

data class GemPackage(
    val id: String,
    val name: String,
    val gemAmount: Int,
    val price: Double,
    val currency: String,
    val imageReference: String
)
