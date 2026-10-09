package pe.greenminds.ecomind.monetization.infrastructure.local

import kotlinx.coroutines.delay
import pe.greenminds.ecomind.monetization.domain.model.Cosmetic
import pe.greenminds.ecomind.monetization.domain.model.GemPackage
import pe.greenminds.ecomind.monetization.domain.model.Multiplier
import pe.greenminds.ecomind.monetization.domain.model.StreakProtector
import pe.greenminds.ecomind.monetization.domain.model.UserCosmetic
import pe.greenminds.ecomind.monetization.domain.repositories.StoreRepository
import pe.greenminds.ecomind.monetization.infrastructure.remote.CosmeticDto
import pe.greenminds.ecomind.monetization.infrastructure.remote.StoreCatalogDto
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
        private const val ALEX_ID = "c0000000-0000-0000-0000-000000000005"
        private const val MICKEY_ID = "c0000000-0000-0000-0000-000000000006"
        private const val RANMA_ID = "c0000000-0000-0000-0000-000000000007"
        private const val ROSALINA_ID = "c0000000-0000-0000-0000-000000000008"
        private const val SONIC_ID = "c0000000-0000-0000-0000-000000000009"
        private const val BUN_ID = "c0000000-0000-0000-0000-000000000010"
        private const val OBSERVATORY_ID = "c0000000-0000-0000-0000-000000000011"
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
                imageReference = "cosmetic_hat"
            ),
            CosmeticDto(
                id = LEAF_WINGS_ID,
                name = "Leaf Wings",
                description = "Avatar with wings made of leaves.",
                priceInGems = 100,
                type = "AVATAR",
                imageReference = "avatar_leafwings"
            ),
            CosmeticDto(
                id = LUNA_LUNETTE_ID,
                name = "Luna Lunette",
                description = "Avatar of Luna.",
                priceInGems = 100,
                type = "AVATAR",
                imageReference = "avatar_lunalunette"
            ),
            CosmeticDto(
                id = SAL_SOLCITO_ID,
                name = "Sal solcito",
                description = "Avatar of Sal.",
                priceInGems = 100,
                type = "AVATAR",
                imageReference = "avatar_salsolcito"
            ),
            CosmeticDto(
                id = ALEX_ID,
                name = "Alex",
                description = "Alex avatar.",
                priceInGems = 150,
                type = "AVATAR",
                imageReference = "avatar_alex"
            ),
            CosmeticDto(
                id = MICKEY_ID,
                name = "Mickey",
                description = "Mickey avatar.",
                priceInGems = 200,
                type = "AVATAR",
                imageReference = "avatar_mickey"
            ),
            CosmeticDto(
                id = RANMA_ID,
                name = "Ranma",
                description = "Ranma avatar.",
                priceInGems = 200,
                type = "AVATAR",
                imageReference = "avatar_ranma"
            ),
            CosmeticDto(
                id = ROSALINA_ID,
                name = "Rosalina",
                description = "Rosalina avatar.",
                priceInGems = 250,
                type = "AVATAR",
                imageReference = "avatar_rosalina"
            ),
            CosmeticDto(
                id = SONIC_ID,
                name = "Sonic",
                description = "Fast blue avatar.",
                priceInGems = 120,
                type = "AVATAR",
                imageReference = "avatar_sonic"
            ),
            CosmeticDto(
                id = BUN_ID,
                name = "Eco Bun",
                description = "Eco-friendly bun for your avatar.",
                priceInGems = 60,
                type = "HEAD",
                imageReference = "cosmetic_bun"
            ),
            CosmeticDto(
                id = OBSERVATORY_ID,
                name = "Observatory",
                description = "Observatory accessory for your avatar.",
                priceInGems = 180,
                type = "ACCESSORY",
                imageReference = "cosmetic_observatorio"
            )
        )
    )

    // One cosmetic in the inventory and one equipped, as in the design
    private val inventory = mutableListOf(
        UserCosmetic(
            cosmeticId = ECO_HAT_ID,
            equipped = false
        ),
        UserCosmetic(
            cosmeticId = LEAF_WINGS_ID,
            equipped = true
        )
    )

    override suspend fun getCosmetics(): Result<List<Cosmetic>> {
        // Simulates the time a request to the web services would take
        delay(SIMULATED_DELAY_MILLIS)
        // Same mapper the remote implementation will use
        return Result.success(catalog.toDomain())
    }

    override suspend fun getUserCosmetics(): Result<List<UserCosmetic>> {
        return Result.success(inventory.toList())
    }

    override suspend fun purchaseCosmetic(cosmeticId: String): Result<Unit> {
        if (catalog.cosmetics.none { it.id == cosmeticId }) {
            return Result.failure(IllegalArgumentException("Cosmetic was not found"))
        }
        if (inventory.any { it.cosmeticId == cosmeticId }) {
            return Result.failure(IllegalArgumentException("Cosmetic already owned"))
        }
        inventory += UserCosmetic(cosmeticId = cosmeticId, equipped = false)
        return Result.success(Unit)
    }

    override suspend fun setCosmeticEquipped(
        cosmeticId: String,
        equipped: Boolean
    ): Result<Unit> {
        val ownedIndex = inventory.indexOfFirst { it.cosmeticId == cosmeticId }
        if (ownedIndex == -1) {
            return Result.failure(IllegalArgumentException("Cosmetic is not in the inventory"))
        }

        val target = catalog.cosmetics.first { it.id == cosmeticId }
        if (equipped) {
            val targetIsAvatar = target.type == "AVATAR"
            inventory.replaceAll { owned ->
                val ownedType = catalog.cosmetics.first { it.id == owned.cosmeticId }.type
                val sameSlot = (ownedType == "AVATAR") == targetIsAvatar
                if (sameSlot) owned.copy(equipped = false) else owned
            }
        }
        inventory[ownedIndex] = inventory[ownedIndex].copy(equipped = equipped)
        return Result.success(Unit)
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

    override suspend fun getGemPackages(): Result<List<GemPackage>> {
        return Result.success(
            listOf(
                GemPackage(
                    id = "40000000-0000-0000-0000-000000000001",
                    name = "Eco Pack 500",
                    gemAmount = 500,
                    price = 5.99,
                    currency = "PEN",
                    imageReference = "gem_pack_0"
                ),
                GemPackage(
                    id = "40000000-0000-0000-0000-000000000002",
                    name = "Eco Pack 1000",
                    gemAmount = 1_000,
                    price = 10.99,
                    currency = "PEN",
                    imageReference = "gem_pack_1"
                ),
                GemPackage(
                    id = "40000000-0000-0000-0000-000000000003",
                    name = "Eco Pack 2000",
                    gemAmount = 2_000,
                    price = 21.99,
                    currency = "PEN",
                    imageReference = "gem_pack_2"
                ),
                GemPackage(
                    id = "40000000-0000-0000-0000-000000000004",
                    name = "Eco Pack 5000",
                    gemAmount = 5_000,
                    price = 65.99,
                    currency = "PEN",
                    imageReference = "gem_pack_3"
                ),
                GemPackage(
                    id = "40000000-0000-0000-0000-000000000005",
                    name = "Mega Bundle",
                    gemAmount = 10_000,
                    price = 149.99,
                    currency = "PEN",
                    imageReference = "gem_pack_4"
                )
            )
        )
    }
}
