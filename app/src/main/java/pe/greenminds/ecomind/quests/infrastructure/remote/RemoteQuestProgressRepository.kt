package pe.greenminds.ecomind.quests.infrastructure.remote
import pe.greenminds.ecomind.quests.domain.repositories.QuestProgressRepository
import pe.greenminds.ecomind.quests.domain.model.QuestStatus
import pe.greenminds.ecomind.users.interfaces.acl.UsersContextFacade
import pe.greenminds.ecomind.shared.infrastructure.remote.RemoteAccess
import retrofit2.HttpException
import javax.inject.Inject
class RemoteQuestProgressRepository @Inject constructor(private val api: QuestApi, private val access: RemoteAccess,
    private val users: UsersContextFacade) : QuestProgressRepository {
    override suspend fun getQuestUsers(userId: Long, status: QuestStatus) = access.authenticated { session, token ->
        check(userId == session.accountId)
        api.byStatus(token, status.name).onEach { check(it.userId == userId) }.map { it.toDomain() }
    }
    override suspend fun getActiveFamilyPlan(userId: Long) = access.authenticated { session, token ->
        check(userId == session.accountId)
        users.getFamilyIdOf(userId)?.let { id ->
            try { api.familyPlan(token, id).also { check(it.familyId == id) }.toDomain() }
            catch (e: HttpException) { if (e.code() == 404) null else throw e }
        }
    }
}
