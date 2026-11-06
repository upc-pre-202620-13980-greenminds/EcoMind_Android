package pe.greenminds.ecomind.learning.domain

enum class MaterialType { TEXT, VIDEO, INFOGRAPHIC }
enum class MaterialCategory { RECYCLE, ENERGY, WATER, ENVIRONMENT }
enum class RecommendedAge { CHILDREN, TEENS, ALL }

data class EducationalMaterial(
    val id: Long,
    val title: String,
    val description: String,
    val content: String,
    val materialType: MaterialType,
    val category: MaterialCategory,
    val imageUrl: String?,
    val videoUrl: String?,
    val durationMinutes: Int,
    val downloadable: Boolean,
    val favorite: Boolean,
    val sourceName: String?,
    val sourceUrl: String?,
    val author: String?,
    val license: String?,
    val recommendedAge: RecommendedAge?
)
