package pe.greenminds.ecomind.quests.infrastructure.implementation

import javax.inject.Inject
import pe.greenminds.ecomind.quests.domain.repositories.QuestExecutionRepository
import pe.greenminds.ecomind.quests.infrastructure.remote.*
import pe.greenminds.ecomind.quests.infrastructure.mapper.toDomain
import retrofit2.HttpException

class RemoteQuestExecutionRepository @Inject constructor(private val api: QuestExecutionApi) : QuestExecutionRepository {
    override suspend fun find(questId: Long) = api.find(questId).let {
        when { it.code() == 404 -> null; !it.isSuccessful -> throw HttpException(it)
            else -> requireNotNull(it.body()).toDomain() }
    }
    override suspend fun start(questId: Long) = api.start(CreateQuestUserRequest(questId)).toDomain()
    override suspend fun get(id: Long) = api.get(id).toDomain()
    override suspend fun activities(questId: Long) = api.activities(questId).map { it.toDomain() }.sortedBy { it.order }
    override suspend fun activityUsers(questUserId: Long) = api.activityUsers(questUserId).map { it.toDomain() }
    override suspend fun check(activityUserId: Long, checked: Boolean) = api.check(activityUserId, CheckboxRequest(mapOf("checked" to checked))).toDomain()
    override suspend fun finish(questUserId: Long) = api.finish(questUserId).toDomain()
}
