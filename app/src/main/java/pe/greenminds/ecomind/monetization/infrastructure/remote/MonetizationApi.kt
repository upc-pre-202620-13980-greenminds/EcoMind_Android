package pe.greenminds.ecomind.monetization.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface MonetizationApi {
    @GET("monetization/cosmetics")
    suspend fun getCosmetics(): Response<List<CosmeticDto>>

    @GET("monetization/me/inventory")
    suspend fun getInventory(): Response<InventoryDto>

    @GET("monetization/me/wallet")
    suspend fun getWallet(): Response<GemWalletDto>

    @GET("monetization/multipliers")
    suspend fun getMultipliers(): Response<List<MultiplierDto>>

    @GET("monetization/streak-protectors")
    suspend fun getStreakProtectors(): Response<List<StreakProtectorDto>>

    @GET("monetization/gem-packages")
    suspend fun getGemPackages(): Response<List<GemPackageDto>>

    @POST("monetization/me/cosmetics/purchases")
    suspend fun purchaseCosmetic(@Body request: BuyItemDto): Response<UserCosmeticDto>

    @POST("monetization/me/multipliers/purchases")
    suspend fun purchaseMultiplier(@Body request: BuyItemDto): Response<OwnedMultiplierDto>

    @POST("monetization/me/protectors/purchases")
    suspend fun purchaseStreakProtector(@Body request: BuyItemDto): Response<OwnedProtectorDto>

    @PUT("monetization/me/cosmetics/{cosmeticId}/equipped")
    suspend fun equipCosmetic(@Path("cosmeticId") cosmeticId: String): Response<Unit>

    @DELETE("monetization/me/cosmetics/{cosmeticId}/equipped")
    suspend fun unequipCosmetic(@Path("cosmeticId") cosmeticId: String): Response<Unit>
}
