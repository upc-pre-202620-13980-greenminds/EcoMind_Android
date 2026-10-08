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

    suspend fun getMultipliers(): Result<List<Multiplier>>

    suspend fun getStreakProtectors(): Result<List<StreakProtector>>

    suspend fun getGemPackages(): Result<List<GemPackage>>
}
