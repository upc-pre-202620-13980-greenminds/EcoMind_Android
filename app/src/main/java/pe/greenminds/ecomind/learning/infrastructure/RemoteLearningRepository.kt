package pe.greenminds.ecomind.learning.infrastructure

import kotlinx.coroutines.CancellationException
import pe.greenminds.ecomind.learning.domain.*
import retrofit2.Response
import javax.inject.Inject

class RemoteLearningRepository @Inject constructor(private val api: LearningApi) : LearningRepository {
    override suspend fun getMaterials(query: String, category: MaterialCategory?, type: MaterialType?) = execute {
        api.getMaterials(query.takeIf(String::isNotBlank), category?.name, type?.name)
    }.map { items -> items.map { it.toDomain() } }

    override suspend fun getMaterial(id: Long) = execute { api.getMaterial(id) }.map { it.toDomain() }
    override suspend fun getFavorites() = execute { api.getFavorites() }.map { items -> items.map { it.toDomain() } }
    override suspend fun toggleFavorite(id: Long) = execute { api.toggleFavorite(id) }.map(FavoriteStatusDto::favorite)

    private suspend fun <T : Any> execute(block: suspend () -> Response<T>): Result<T> = try {
        val response = block()
        if (response.isSuccessful) Result.success(requireNotNull(response.body()))
        else Result.failure(IllegalStateException(responseMessage(response.code())))
    } catch (e: CancellationException) { throw e } catch (e: Exception) { Result.failure(e) }

    private fun responseMessage(code: Int) = when (code) {
        401, 403 -> "Tu sesión venció. Inicia sesión nuevamente."
        404 -> "El servicio de aprendizaje aún no está disponible. Reinicia el backend."
        in 500..599 -> "El servidor no pudo cargar los materiales."
        else -> "No se pudieron cargar los materiales educativos."
    }

    private fun EducationalMaterialDto.toDomain() = EducationalMaterial(
        id, title, description, content, MaterialType.valueOf(materialType),
        MaterialCategory.valueOf(category), imageUrl, videoUrl, durationMinutes, downloadable, favorite
    )
}
