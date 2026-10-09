package pe.greenminds.ecomind.quests.infrastructure.implementation

import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import pe.greenminds.ecomind.quests.domain.entity.Quest
import pe.greenminds.ecomind.quests.domain.repositories.QuestRepository
import pe.greenminds.ecomind.quests.domain.valueobject.QuestCategory
import pe.greenminds.ecomind.quests.domain.valueobject.QuestType
import pe.greenminds.ecomind.quests.infrastructure.mapper.toDomain
import pe.greenminds.ecomind.quests.infrastructure.remote.QuestApi
import pe.greenminds.ecomind.shared.infrastructure.remote.ErrorDto
import pe.greenminds.ecomind.shared.infrastructure.remote.toException
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject

class RemoteQuestRepository @Inject constructor(private val api: QuestApi) : QuestRepository {
    override suspend fun getQuests(): Result<List<Quest>> = execute(
        request = { api.getQuests() }, transform = { dtos -> dtos.map { it.toDomain() } }
    )

    override suspend fun searchQuests(
        query: String, category: QuestCategory?, questType: QuestType?
    ): Result<List<Quest>> = execute(
        request = { api.searchQuests(query, category?.name, questType?.name) },
        transform = { dtos -> dtos.map { it.toDomain() } }
    )

    override suspend fun getQuest(questId: Long): Result<Quest> = execute(
        request = { api.getQuest(questId) }, transform = { it.toDomain() }
    )

    private suspend fun <Dto : Any, Model> execute(
        request: suspend () -> Response<Dto>, transform: (Dto) -> Model
    ): Result<Model> = try {
        val response = request()
        if (!response.isSuccessful) {
            val error = try {
                response.errorBody()?.use { Gson().fromJson(it.string(), ErrorDto::class.java) }
            } catch (_: Exception) { null }
            Result.failure(error?.toException() ?: HttpException(response))
        } else {
            val body = response.body() ?: throw IllegalStateException("Empty quest response")
            Result.success(transform(body))
        }
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}
