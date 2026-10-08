package pe.greenminds.ecomind.monetization.application

import pe.greenminds.ecomind.monetization.domain.model.CosmeticOwnership
import pe.greenminds.ecomind.monetization.domain.model.StoreItem
import pe.greenminds.ecomind.monetization.domain.repositories.StoreRepository
import javax.inject.Inject

class GetStoreItemsUseCase @Inject constructor(private val repository: StoreRepository) {

    suspend operator fun invoke(): Result<List<StoreItem>> {
        val cosmetics = repository.getCosmetics()
            .getOrElse { return Result.failure(it) }
        val owned = repository.getUserCosmetics()
            .getOrElse { return Result.failure(it) }

        // The catalog does not say what the user owns, so both answers are crossed here
        val items = cosmetics.map { cosmetic ->
            val userCosmetic = owned.find { it.cosmeticId == cosmetic.id }
            val ownership = when {
                userCosmetic == null -> CosmeticOwnership.NOT_OWNED
                userCosmetic.equipped -> CosmeticOwnership.EQUIPPED
                else -> CosmeticOwnership.IN_INVENTORY
            }
            StoreItem(cosmetic, ownership)
        }
        return Result.success(items)
    }
}
