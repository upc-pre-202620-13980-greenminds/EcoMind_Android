package pe.greenminds.ecomind.monetization.domain.model

// Slot in which a cosmetic is shown in the profile
enum class CosmeticType {
    AVATAR,
    HEAD,
    BODY,
    ACCESSORY
}

data class Cosmetic(
    // UUID
    val id: String,
    val name: String,
    val description: String,
    val priceInGems: Int,
    val type: CosmeticType,
    val imageReference: String?
)

// A cosmetic the user already bought
data class UserCosmetic(
    val cosmeticId: String,
    val equipped: Boolean
)

enum class CosmeticOwnership {
    NOT_OWNED,
    IN_INVENTORY,
    EQUIPPED
}

// A cosmetic of the catalog together with what it is for the user
data class StoreItem(
    val cosmetic: Cosmetic,
    val ownership: CosmeticOwnership
)
