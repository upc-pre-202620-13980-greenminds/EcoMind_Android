package pe.greenminds.ecomind.monetization.domain.repositories

import pe.greenminds.ecomind.monetization.domain.model.Cosmetic
import pe.greenminds.ecomind.monetization.domain.model.GemPackage
import pe.greenminds.ecomind.monetization.domain.model.Multiplier
import pe.greenminds.ecomind.monetization.domain.model.StreakProtector
import pe.greenminds.ecomind.monetization.domain.model.UserCosmetic

interface StoreRepository {

    // Cosmetics of the store catalog
    suspend fun getCosmetics(): Result<List<Cosmetic>>

    // Cosmetics the authenticated user owns
    suspend fun getUserCosmetics(): Result<List<UserCosmetic>>

    // Local equivalents of the protected purchase/equip endpoints.
    suspend fun purchaseCosmetic(cosmeticId: String): Result<Unit>

    suspend fun setCosmeticEquipped(cosmeticId: String, equipped: Boolean): Result<Unit>

    suspend fun getMultipliers(): Result<List<Multiplier>>

    suspend fun getStreakProtectors(): Result<List<StreakProtector>>

    suspend fun getGemPackages(): Result<List<GemPackage>>
}
