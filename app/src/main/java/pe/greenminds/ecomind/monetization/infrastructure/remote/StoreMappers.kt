package pe.greenminds.ecomind.monetization.infrastructure.remote

import pe.greenminds.ecomind.monetization.domain.model.Cosmetic
import pe.greenminds.ecomind.monetization.domain.model.CosmeticType
import pe.greenminds.ecomind.monetization.domain.model.UserCosmetic

fun CosmeticDto.toDomain(): Cosmetic {
    return Cosmetic(
        id = id,
        name = name,
        description = description,
        priceInGems = priceInGems,
        type = CosmeticType.valueOf(type),
        imageReference = imageReference
    )
}

fun StoreCatalogDto.toDomain(): List<Cosmetic> = cosmetics.map { it.toDomain() }

fun UserCosmeticDto.toDomain(): UserCosmetic {
    return UserCosmetic(
        cosmeticId = cosmeticId,
        equipped = equipped
    )
}

fun InventoryDto.toDomain(): List<UserCosmetic> = cosmetics.map { it.toDomain() }
