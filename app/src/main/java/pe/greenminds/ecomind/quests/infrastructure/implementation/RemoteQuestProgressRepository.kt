package pe.greenminds.ecomind.quests.infrastructure.implementation

import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import pe.greenminds.ecomind.quests.domain.repositories.QuestProgressRepository
import pe.greenminds.ecomind.quests.domain.repositories.QuestRepository
import pe.greenminds.ecomind.quests.domain.valueobject.QuestStatus
import pe.greenminds.ecomind.quests.infrastructure.mapper.toDomain
import pe.greenminds.ecomind.quests.infrastructure.remote.QuestExecutionApi
import pe.greenminds.ecomind.shared.infrastructure.remote.ApiException
import pe.greenminds.ecomind.shared.infrastructure.remote.ErrorDto
import pe.greenminds.ecomind.shared.infrastructure.remote.toException
import retrofit2.HttpException
import javax.inject.Inject

class RemoteQuestProgressRepository @Inject constructor(
    private val api: QuestExecutionApi,
    private val quests: QuestRepository
) : QuestProgressRepository {
    override suspend fun getQuestUsers() = request {
        // IN_PROGRESS also initializes today's daily quest. Read it before the other statuses.
        listOf(QuestStatus.IN_PROGRESS, QuestStatus.READY_TO_COMPLETE, QuestStatus.COMPLETED)
            .flatMap { api.getByStatus(it.name) }
            .map { it.toDomain() }.associateBy { it.id }.values.toList()
    }

    override suspend fun getQuests(questIds: Set<Long>) = request {
        val concurrency = Semaphore(4)
        coroutineScope {
            questIds.map { id ->
                async {
                    concurrency.withPermit {
                        val result = quests.getQuest(id)
                        val error = result.exceptionOrNull()
                        when {
                            error is HttpException && error.code() == 404 -> null
                            error is ApiException && error.code.endsWith("NOT_FOUND") -> null
                            else -> result.getOrThrow()
                        }
                    }
                }
            }.awaitAll().filterNotNull()
        }
    }

    override suspend fun getActivityUsers(questUserId: Long) = request {
        api.activityUsers(questUserId).map { it.toDomain() }
    }

    private suspend fun <T> request(action: suspend () -> T): Result<T> = try {
        Result.success(action())
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: HttpException) {
        val error = try {
            exception.response()?.errorBody()?.use { Gson().fromJson(it.string(), ErrorDto::class.java) }
        } catch (_: Exception) { null }
        Result.failure(error?.toException() ?: exception)
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}
