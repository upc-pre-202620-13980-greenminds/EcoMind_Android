package pe.greenminds.ecomind.monetization.application

import pe.greenminds.ecomind.monetization.domain.repositories.StoreRepository
import javax.inject.Inject

class SetCosmeticEquippedUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    suspend operator fun invoke(cosmeticId: String, equipped: Boolean): Result<Unit> =
        repository.setCosmeticEquipped(cosmeticId, equipped)
}
