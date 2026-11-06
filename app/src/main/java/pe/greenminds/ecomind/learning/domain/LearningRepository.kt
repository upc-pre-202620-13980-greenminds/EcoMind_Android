package pe.greenminds.ecomind.learning.domain

interface LearningRepository {
    suspend fun getMaterials(query: String = "", category: MaterialCategory? = null,
        type: MaterialType? = null, recommendedAge: RecommendedAge? = null,
        minDurationMinutes: Int? = null, maxDurationMinutes: Int? = null): Result<List<EducationalMaterial>>
    suspend fun getMaterial(id: Long): Result<EducationalMaterial>
    suspend fun getFavorites(): Result<List<EducationalMaterial>>
    suspend fun getRecent(): Result<List<EducationalMaterial>>
    suspend fun toggleFavorite(id: Long): Result<Boolean>
}
