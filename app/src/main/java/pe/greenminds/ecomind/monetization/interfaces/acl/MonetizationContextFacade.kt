package pe.greenminds.ecomind.monetization.interfaces.acl

import pe.greenminds.ecomind.monetization.domain.model.CosmeticType
import pe.greenminds.ecomind.monetization.domain.model.EquippedCosmetics
import pe.greenminds.ecomind.monetization.application.GetStoreItemsUseCase
import pe.greenminds.ecomind.monetization.domain.model.CosmeticOwnership
import javax.inject.Inject

class MonetizationContextFacade @Inject constructor(
    private val getStoreItems: GetStoreItemsUseCase
) {
    suspend fun getEquippedCosmetics(): EquippedCosmetics {
        val equipped = getStoreItems().getOrDefault(emptyList())
            .filter { it.ownership == CosmeticOwnership.EQUIPPED }
            .associate { it.cosmetic.type to it.cosmetic }

        return EquippedCosmetics(
            avatar = equipped[CosmeticType.AVATAR],
            head = equipped[CosmeticType.HEAD],
            body = equipped[CosmeticType.BODY],
            accessory = equipped[CosmeticType.ACCESSORY]
        )
    }
}
