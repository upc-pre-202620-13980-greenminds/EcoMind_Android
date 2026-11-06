package pe.greenminds.ecomind.monetization.application

import pe.greenminds.ecomind.monetization.domain.model.GemPurchase
import pe.greenminds.ecomind.monetization.domain.model.PaymentMethod
import pe.greenminds.ecomind.monetization.domain.repositories.StoreRepository
import javax.inject.Inject

class PurchaseGemPackageUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    suspend operator fun invoke(
        packageId: String,
        paymentMethod: PaymentMethod,
        sourceToken: String,
        email: String
    ): Result<GemPurchase> = repository.purchaseGemPackage(
        packageId = packageId,
        paymentMethod = paymentMethod,
        sourceToken = sourceToken,
        email = email
    )
}
