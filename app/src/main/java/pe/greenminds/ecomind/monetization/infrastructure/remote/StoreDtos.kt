package pe.greenminds.ecomind.monetization.infrastructure.remote

// GET /api/v1/monetization/store
data class StoreCatalogDto(
    val cosmetics: List<CosmeticDto>
)

data class CosmeticDto(
    // UUID
    val id: String,
    val name: String,
    val description: String,
    val priceInGems: Int,
    val type: String,
    val imageReference: String?
)

// GET /api/v1/monetization/me/inventory
// The response also has protectors and multipliers, not declared for the same reason.
data class InventoryDto(
    val cosmetics: List<UserCosmeticDto>,
    val protectors: List<OwnedProtectorDto> = emptyList(),
    val multipliers: List<OwnedMultiplierDto> = emptyList()
)

data class UserCosmeticDto(
    // UUID
    val id: String,
    // UUID
    val cosmeticId: String,
    val equipped: Boolean
)

data class OwnedProtectorDto(
    val id: String,
    val protectorId: String,
    val quantity: Int
)

data class OwnedMultiplierDto(
    val id: String,
    val multiplierId: String,
    val factor: Double,
    val startsAt: String,
    val expiresAt: String
)

data class BuyItemDto(
    val itemId: String,
    val requestId: String
)

data class GemWalletDto(val balance: Int)

data class MultiplierDto(
    val id: String,
    val name: String,
    val description: String,
    val factor: Double,
    val durationMinutes: Int,
    val priceInGems: Int
)

data class StreakProtectorDto(
    val id: String,
    val name: String,
    val description: String,
    val priceInGems: Int
)

data class GemPackageDto(
    val id: String,
    val name: String,
    val gemAmount: Int,
    val price: Double,
    val currency: String
)
