package pe.greenminds.ecomind.monetization.infrastructure.remote

// PROVISIONAL: these contracts come from a branch of the web services that is not merged yet.
// They must be checked again when Monetization reaches the main branch.

// GET /api/v1/monetization/store
// The response also has multipliers, streakProtectors and gemPackages; they belong to
// tabs that are not built yet, so they are not declared here.
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
    val cosmetics: List<UserCosmeticDto>
)

data class UserCosmeticDto(
    // UUID
    val id: String,
    val userId: Long,
    // UUID
    val cosmeticId: String,
    val equipped: Boolean
)
