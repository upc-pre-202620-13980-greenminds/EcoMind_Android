package pe.greenminds.ecomind.quests.infrastructure.remote
import pe.greenminds.ecomind.quests.domain.repositories.QuestRepository
import pe.greenminds.ecomind.quests.domain.model.QuestFilter
import pe.greenminds.ecomind.shared.infrastructure.remote.RemoteAccess
import javax.inject.Inject
class RemoteQuestRepository @Inject constructor(private val api: QuestApi, private val access: RemoteAccess) : QuestRepository {
    override suspend fun searchQuests(filter: QuestFilter) = access.authenticated { _, token ->
        api.search(token, filter.title, filter.category?.name, filter.questType?.name).map { it.toDomain() }
    }
    override suspend fun getQuest(questId: Long) = access.authenticated { _, token -> api.quest(token, questId).toDomain() }
}
