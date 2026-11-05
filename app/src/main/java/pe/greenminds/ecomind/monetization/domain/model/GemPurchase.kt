package pe.greenminds.ecomind.monetization.domain.model

enum class PaymentMethod { CARD, YAPE, PAYPAL }

enum class PaymentStatus { PENDING, APPROVED, REJECTED }

data class GemPurchase(
    val id: String,
    val packageId: String,
    val gemAmount: Int,
    val amountPaid: Double,
    val currency: String,
    val paymentMethod: PaymentMethod,
    val paymentStatus: PaymentStatus,
    val paymentReference: String?
)
