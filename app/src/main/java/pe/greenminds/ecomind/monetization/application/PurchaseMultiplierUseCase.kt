package pe.greenminds.ecomind.monetization.application

import pe.greenminds.ecomind.monetization.domain.repositories.StoreRepository
import javax.inject.Inject

class PurchaseMultiplierUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    suspend operator fun invoke(multiplierId: String): Result<Unit> =
        repository.purchaseMultiplier(multiplierId)
}
