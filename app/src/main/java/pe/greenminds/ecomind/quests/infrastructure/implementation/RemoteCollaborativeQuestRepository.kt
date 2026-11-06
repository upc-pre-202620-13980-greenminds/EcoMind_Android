package pe.greenminds.ecomind.quests.infrastructure.implementation

import com.google.gson.Gson
import kotlinx.coroutines.CancellationException
import pe.greenminds.ecomind.quests.domain.repositories.CollaborativeQuestRepository
import pe.greenminds.ecomind.quests.infrastructure.mapper.toDomain
import pe.greenminds.ecomind.quests.infrastructure.remote.*
import pe.greenminds.ecomind.shared.infrastructure.remote.ErrorDto
import pe.greenminds.ecomind.shared.infrastructure.remote.toException
import retrofit2.HttpException
import retrofit2.Response
import javax.inject.Inject

class RemoteCollaborativeQuestRepository @Inject constructor(
    private val api: CollaborativeQuestApi
) : CollaborativeQuestRepository {
    override suspend fun acceptedMemberships() = execute({ api.memberships("ACCEPTED") }) { body -> requireNotNull(body).map { it.toDomain() } }
    override suspend fun getInvitations() = execute({ api.getInvitations() }) { body ->
        requireNotNull(body).map { it.toDomain() }
    }
    override suspend fun getStateByQuest(questId: Long) = execute({ api.getStateByQuest(questId) }) { requireNotNull(it).toDomain() }
    override suspend fun getStateBySession(sessionId: Long) = execute({ api.getStateBySession(sessionId) }) { requireNotNull(it).toDomain() }
    override suspend fun createParty(questId: Long) = execute({ api.createParty(CreatePartyRequest(questId)) }) { requireNotNull(it).toDomain() }
    override suspend fun startParty(sessionId: Long) = execute({ api.startParty(sessionId) }) { Unit }
    override suspend fun cancelParty(sessionId: Long) = execute({ api.cancelParty(sessionId) }) { Unit }
    override suspend fun invite(sessionId: Long, userId: Long) = execute({ api.invite(InvitePartyMemberRequest(sessionId, userId)) }) { Unit }
    override suspend fun accept(memberId: Long) = execute({ api.accept(memberId) }) { Unit }
    override suspend fun decline(memberId: Long) = execute({ api.decline(memberId) }) { Unit }
    override suspend fun leave(memberId: Long) = execute({ api.leave(memberId) }) { Unit }
    override suspend fun remove(memberId: Long) = execute({ api.remove(memberId) }) { Unit }

    private suspend fun <Dto, Model> execute(
        request: suspend () -> Response<Dto>,
        transform: (Dto?) -> Model
    ): Result<Model> = try {
        val response = request()
        if (response.isSuccessful) {
            Result.success(transform(response.body()))
        } else {
            val error = try {
                response.errorBody()?.use { Gson().fromJson(it.string(), ErrorDto::class.java) }
            } catch (_: Exception) { null }
            Result.failure(error?.toException() ?: HttpException(response))
        }
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}
