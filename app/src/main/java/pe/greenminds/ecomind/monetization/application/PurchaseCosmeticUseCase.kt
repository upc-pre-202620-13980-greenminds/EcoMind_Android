package pe.greenminds.ecomind.monetization.application

import pe.greenminds.ecomind.monetization.domain.repositories.StoreRepository
import javax.inject.Inject

class PurchaseCosmeticUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    suspend operator fun invoke(cosmeticId: String): Result<Unit> =
        repository.purchaseCosmetic(cosmeticId)
}
