package pe.greenminds.ecomind.monetization.interfaces.acl

import pe.greenminds.ecomind.monetization.domain.model.CosmeticType
import pe.greenminds.ecomind.monetization.domain.model.EquippedCosmetics
import pe.greenminds.ecomind.monetization.domain.repositories.StoreRepository
import javax.inject.Inject

class MonetizationContextFacade @Inject constructor(
    private val repository: StoreRepository
) {
    suspend fun getEquippedCosmetics(): EquippedCosmetics {
        val catalog = repository.getCosmetics().getOrDefault(emptyList())
        val equippedIds = repository.getUserCosmetics()
            .getOrDefault(emptyList())
            .filter { it.equipped }
            .mapTo(mutableSetOf()) { it.cosmeticId }

        return EquippedCosmetics(
            avatar = catalog.firstOrNull {
                it.id in equippedIds && it.type == CosmeticType.AVATAR
            },
            overlay = catalog.firstOrNull {
                it.id in equippedIds && it.type != CosmeticType.AVATAR
            }
        )
    }
}
