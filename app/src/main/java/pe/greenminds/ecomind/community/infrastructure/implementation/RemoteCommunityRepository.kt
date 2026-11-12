package pe.greenminds.ecomind.community.infrastructure.implementation

import com.google.gson.Gson
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import pe.greenminds.ecomind.community.domain.repositories.CommunityRepository
import pe.greenminds.ecomind.community.infrastructure.mapper.*
import pe.greenminds.ecomind.community.infrastructure.remote.CommunityApi
import pe.greenminds.ecomind.shared.infrastructure.remote.ErrorDto
import pe.greenminds.ecomind.shared.infrastructure.remote.toException
import retrofit2.HttpException
import retrofit2.Response

class RemoteCommunityRepository @Inject constructor(
    private val api: CommunityApi
) : CommunityRepository {

    override suspend fun getEvents(communityId: Long) =
        execute({ api.getEvents(communityId) }) { list -> list.map { it.toDomain() } }

    override suspend fun getPosts(communityId: Long) =
        execute({ api.getPosts(communityId) }) { list -> list.map { it.toDomain() } }

    override suspend fun getAchievements(communityId: Long) =
        execute({ api.getAchievements(communityId) }) { list -> list.map { it.toDomain() } }

    override suspend fun getGoals(communityId:Long) =
        execute({ api.getCommunityGoals(communityId) }) { list -> list.map { it.toDomain() } }

    private suspend fun <Dto : Any, Model> execute(
        request: suspend () -> Response<Dto>,
        transform: (Dto) -> Model
    ): Result<Model> = try {
        val response = request()

        if (!response.isSuccessful) {
            val error = try {
                response.errorBody()
                    ?.use { Gson().fromJson(it.string(), ErrorDto::class.java) }
            } catch (_: Exception) {
                null
            }
            Result.failure(error?.toException() ?: HttpException(response))
        } else {
            val body = response.body()
                ?: throw IllegalStateException("Community response is empty")
            Result.success(transform(body))
        }
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}