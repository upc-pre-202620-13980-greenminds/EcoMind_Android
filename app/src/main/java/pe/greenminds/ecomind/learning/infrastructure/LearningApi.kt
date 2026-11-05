package pe.greenminds.ecomind.learning.infrastructure

import retrofit2.Response
import retrofit2.http.*

data class EducationalMaterialDto(
    val id: Long,
    val title: String,
    val description: String,
    val content: String,
    val materialType: String,
    val category: String,
    val imageUrl: String?,
    val videoUrl: String?,
    val durationMinutes: Int,
    val downloadable: Boolean,
    val favorite: Boolean
)

data class FavoriteStatusDto(val favorite: Boolean)

interface LearningApi {
    @GET("educational-materials")
    suspend fun getMaterials(
        @Query("query") query: String?,
        @Query("category") category: String?,
        @Query("materialType") type: String?,
        @Query("language") language: String = "es"
    ): Response<List<EducationalMaterialDto>>

    @GET("educational-materials/{id}") suspend fun getMaterial(@Path("id") id: Long): Response<EducationalMaterialDto>
    @GET("educational-materials/favorites") suspend fun getFavorites(): Response<List<EducationalMaterialDto>>
    @POST("educational-materials/{id}/favorite") suspend fun toggleFavorite(@Path("id") id: Long): Response<FavoriteStatusDto>
}
