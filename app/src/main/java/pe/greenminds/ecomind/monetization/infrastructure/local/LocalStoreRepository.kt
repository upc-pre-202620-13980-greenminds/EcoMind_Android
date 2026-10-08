package pe.greenminds.ecomind.monetization.infrastructure.local

import kotlinx.coroutines.delay
import pe.greenminds.ecomind.monetization.domain.model.Cosmetic
import pe.greenminds.ecomind.monetization.domain.model.Multiplier
import pe.greenminds.ecomind.monetization.domain.model.StreakProtector
import pe.greenminds.ecomind.monetization.domain.model.UserCosmetic
import pe.greenminds.ecomind.monetization.domain.repositories.StoreRepository
import pe.greenminds.ecomind.monetization.infrastructure.remote.CosmeticDto
import pe.greenminds.ecomind.monetization.infrastructure.remote.InventoryDto
import pe.greenminds.ecomind.monetization.infrastructure.remote.StoreCatalogDto
import pe.greenminds.ecomind.monetization.infrastructure.remote.UserCosmeticDto
import pe.greenminds.ecomind.monetization.infrastructure.remote.toDomain
import javax.inject.Inject

// Demo implementation used until the web services are deployed.
// The data is fixed: it shows one card of each kind and nothing changes it.
class LocalStoreRepository @Inject constructor() : StoreRepository {

    companion object {
        private const val SIMULATED_DELAY_MILLIS = 400L

        private const val ECO_HAT_ID = "c0000000-0000-0000-0000-000000000001"
        private const val LEAF_WINGS_ID = "c0000000-0000-0000-0000-000000000002"
        private const val LUNA_LUNETTE_ID = "c0000000-0000-0000-0000-000000000003"
        private const val SAL_SOLCITO_ID = "c0000000-0000-0000-0000-000000000004"
    }

    // The four cosmetics of the design, with the shape of the store catalog
    private val catalog = StoreCatalogDto(
        cosmetics = listOf(
            CosmeticDto(
                id = ECO_HAT_ID,
                name = "Eco Hat",
                description = "Green hat for your avatar.",
                priceInGems = 100,
                type = "HEAD",
                imageReference = null
            ),
            CosmeticDto(
                id = LEAF_WINGS_ID,
                name = "Leaf Wings",
                description = "Avatar with wings made of leaves.",
                priceInGems = 100,
                type = "AVATAR",
                imageReference = null
            ),
            CosmeticDto(
                id = LUNA_LUNETTE_ID,
                name = "Luna Lunette",
                description = "Avatar of Luna.",
                priceInGems = 100,
                type = "AVATAR",
                imageReference = null
            ),
            CosmeticDto(
                id = SAL_SOLCITO_ID,
                name = "Sal solcito",
                description = "Avatar of Sal.",
                priceInGems = 100,
                type = "AVATAR",
                imageReference = null
            )
        )
    )

    // One cosmetic in the inventory and one equipped, as in the design
    private val inventory = InventoryDto(
        cosmetics = listOf(
            UserCosmeticDto(
                id = "a0000000-0000-0000-0000-000000000001",
                userId = 1L,
                cosmeticId = ECO_HAT_ID,
                equipped = false
            ),
            UserCosmeticDto(
                id = "a0000000-0000-0000-0000-000000000002",
                userId = 1L,
                cosmeticId = LEAF_WINGS_ID,
                equipped = true
            )
        )
    )

    override suspend fun getCosmetics(): Result<List<Cosmetic>> {
        // Simulates the time a request to the web services would take
        delay(SIMULATED_DELAY_MILLIS)
        // Same mapper the remote implementation will use
        return Result.success(catalog.toDomain())
    }

    override suspend fun getUserCosmetics(): Result<List<UserCosmetic>> {
        return Result.success(inventory.toDomain())
    }

    override suspend fun getMultipliers(): Result<List<Multiplier>> {
        return Result.success(
            listOf(
                Multiplier(
                    id = "20000000-0000-0000-0000-000000000001",
                    name = "Multiplier x1.5",
                    description = "Receive 1.5x more ecoPoints for 60 minutes.",
                    factor = 1.5,
                    durationMinutes = 60,
                    priceInGems = 100,
                    imageReference = "world_happy"
                ),
                Multiplier(
                    id = "20000000-0000-0000-0000-000000000002",
                    name = "Multiplier x2",
                    description = "Receive 2x more ecoPoints for 60 minutes.",
                    factor = 2.0,
                    durationMinutes = 60,
                    priceInGems = 250,
                    imageReference = "world_run"
                ),
                Multiplier(
                    id = "20000000-0000-0000-0000-000000000003",
                    name = "Multiplier x3",
                    description = "Receive 3x more ecoPoints for 60 minutes.",
                    factor = 3.0,
                    durationMinutes = 60,
                    priceInGems = 700,
                    imageReference = "world_trophy"
                )
            )
        )
    }

    override suspend fun getStreakProtectors(): Result<List<StreakProtector>> {
        return Result.success(
            listOf(
                StreakProtector(
                    id = "30000000-0000-0000-0000-000000000001",
                    name = "Streak protector",
                    description = "Avoid losing your streak for a day.",
                    priceInGems = 150,
                    imageReference = "world_streak_protector"
                )
            )
        )
    }
}
