package pe.greenminds.ecomind.community.infrastructure.implementation

import com.google.gson.Gson
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import pe.greenminds.ecomind.community.domain.entity.Community
import pe.greenminds.ecomind.community.domain.entity.CommunityMembership
import pe.greenminds.ecomind.community.domain.repositories.CommunityDirectoryRepository
import pe.greenminds.ecomind.community.infrastructure.mapper.toDomain
import pe.greenminds.ecomind.community.infrastructure.remote.CommunityApi
import pe.greenminds.ecomind.shared.infrastructure.remote.ErrorDto
import pe.greenminds.ecomind.shared.infrastructure.remote.toException
import retrofit2.HttpException
import retrofit2.Response

class RemoteCommunityDirectoryRepository @Inject constructor(
    private val api: CommunityApi
) : CommunityDirectoryRepository {

    override suspend fun getMyMemberships(): Result<List<CommunityMembership>> =
        execute(
            request = { api.getMyMemberships() },
            transform = { list -> list.map { it.toDomain() } }
        )

    override suspend fun getLocalCommunities(
        locality: String?
    ): Result<List<Community>> =
        execute(
            request = {
                api.getCommunities(
                    type = "local",
                    locality = locality
                )
            },
            transform = { list -> list.map { it.toDomain() } }
        )

    override suspend fun joinCommunity(
        communityId: Long
    ): Result<CommunityMembership> =
        execute(
            request = { api.joinCommunity(communityId) },
            transform = { it.toDomain() }
        )

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
                ?: throw IllegalStateException("Community's empty response")

            Result.success(transform(body))
        }
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}