package pe.greenminds.ecomind.monetization.application

import pe.greenminds.ecomind.monetization.domain.repositories.StoreRepository
import javax.inject.Inject

class PurchaseStreakProtectorUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    suspend operator fun invoke(protectorId: String): Result<Unit> =
        repository.purchaseStreakProtector(protectorId)
}
