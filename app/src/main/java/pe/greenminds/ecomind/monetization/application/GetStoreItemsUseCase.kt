package pe.greenminds.ecomind.monetization.application

import pe.greenminds.ecomind.monetization.domain.model.CosmeticOwnership
import pe.greenminds.ecomind.monetization.domain.model.StoreItem
import com.google.gson.reflect.TypeToken
import pe.greenminds.ecomind.monetization.infrastructure.remote.MonetizationApi
import pe.greenminds.ecomind.monetization.infrastructure.remote.toDomain
import pe.greenminds.ecomind.shared.infrastructure.offline.OfflineSnapshots
import pe.greenminds.ecomind.shared.infrastructure.remote.apiResult
import pe.greenminds.ecomind.shared.infrastructure.remote.requireBody
import javax.inject.Inject

class GetStoreItemsUseCase @Inject constructor(
    private val api: MonetizationApi,
    private val snapshots: OfflineSnapshots
) {
    private val type = object : TypeToken<List<StoreItem>>() {}.type

    suspend fun saved(): List<StoreItem>? =
        snapshots.session()?.let { snapshots.read(it, "store-items", type) }

    suspend operator fun invoke(): Result<List<StoreItem>> =
        snapshots.fetch("store-items", type) { session ->
            apiResult {
                val cosmetics = api.getCosmetics(session).requireBody().map { it.toDomain() }
                val owned = api.getInventory(session).requireBody().toDomain().associateBy { it.cosmeticId }
                cosmetics.map { cosmetic ->
                    val ownership = when (owned[cosmetic.id]?.equipped) {
                        null -> CosmeticOwnership.NOT_OWNED
                        true -> CosmeticOwnership.EQUIPPED
                        false -> CosmeticOwnership.IN_INVENTORY
                    }
                    StoreItem(cosmetic, ownership)
                }
            }
        }
}
