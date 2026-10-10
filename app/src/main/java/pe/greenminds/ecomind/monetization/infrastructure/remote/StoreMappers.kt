package pe.greenminds.ecomind.monetization.infrastructure.remote

import pe.greenminds.ecomind.monetization.domain.model.Cosmetic
import pe.greenminds.ecomind.monetization.domain.model.CosmeticType
import pe.greenminds.ecomind.monetization.domain.model.GemPackage
import pe.greenminds.ecomind.monetization.domain.model.Multiplier
import pe.greenminds.ecomind.monetization.domain.model.StreakProtector
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

fun MultiplierDto.toDomain(): Multiplier = Multiplier(
    id = id,
    name = name,
    description = description,
    factor = factor,
    durationMinutes = durationMinutes,
    priceInGems = priceInGems,
    imageReference = when {
        factor <= 1.5 -> "world_happy"
        factor <= 2.0 -> "world_run"
        else -> "world_trophy"
    }
)

fun StreakProtectorDto.toDomain(): StreakProtector = StreakProtector(
    id = id,
    name = name,
    description = description,
    priceInGems = priceInGems,
    imageReference = "world_streak_protector"
)

fun GemPackageDto.toDomain(): GemPackage = GemPackage(
    id = id,
    name = name,
    gemAmount = gemAmount,
    price = price,
    currency = currency,
    imageReference = when {
        gemAmount >= 10_000 -> "gem_pack_4"
        gemAmount >= 5_000 -> "gem_pack_3"
        gemAmount >= 2_000 -> "gem_pack_2"
        gemAmount >= 1_000 -> "gem_pack_1"
        else -> "gem_pack_0"
    }
)
