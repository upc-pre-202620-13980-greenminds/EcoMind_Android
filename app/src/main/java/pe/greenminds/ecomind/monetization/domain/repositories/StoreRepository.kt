package pe.greenminds.ecomind.monetization.domain.repositories

import pe.greenminds.ecomind.monetization.domain.model.Cosmetic
import pe.greenminds.ecomind.monetization.domain.model.UserCosmetic

interface StoreRepository {

    // Cosmetics of the store catalog
    suspend fun getCosmetics(): Result<List<Cosmetic>>

    // Cosmetics the authenticated user owns
    suspend fun getUserCosmetics(): Result<List<UserCosmetic>>
}
