package pe.greenminds.ecomind.quests.infrastructure.remote

import pe.greenminds.ecomind.quests.domain.model.*
import pe.greenminds.ecomind.quests.domain.repositories.QuestExecutionRepository
import pe.greenminds.ecomind.shared.infrastructure.remote.RemoteAccess
import retrofit2.HttpException
import javax.inject.Inject

class RemoteQuestExecutionRepository @Inject constructor(private val api: QuestApi, private val access: RemoteAccess) : QuestExecutionRepository {
    override suspend fun load(questId: Long) = access.authenticated { session, token -> load(questId, session.accountId, token) }
    override suspend fun start(questId: Long) = access.authenticated { session, token ->
        val before = load(questId, session.accountId, token)
        check(before.canStart)
        // Read before retrying a timed-out create: the service may already have saved it.
        if (before.assignmentId == null) api.start(token, StartQuestBody(questId))
        load(questId, session.accountId, token)
    }
    override suspend fun check(questId: Long, assignmentId: Long, done: Boolean) = access.authenticated { session, token ->
        val before = load(questId, session.accountId, token)
        check(before.status != "COMPLETED" && before.steps.any { it.assignmentId == assignmentId && it.type == "CHECKBOX" })
        api.submit(token, assignmentId, SubmitActivityBody(mapOf("checked" to done)))
        load(questId, session.accountId, token)
    }
    override suspend fun finish(questId: Long) = access.authenticated { session, token ->
        val before = load(questId, session.accountId, token)
        if (before.status != "COMPLETED") {
            check(before.status == "READY_TO_COMPLETE")
            api.finish(token, checkNotNull(before.assignmentId))
        }
        load(questId, session.accountId, token)
    }
    private suspend fun load(questId: Long, userId: Long, token: String): QuestExecution {
        val quest = api.quest(token, questId).toDomain()
        val assignment = try { api.assignment(token, questId) } catch (e: HttpException) { if (e.code() == 404) null else throw e }
        check(assignment == null || (assignment.userId == userId && assignment.questId == questId))
        val steps = assignment?.let { api.steps(token, it.id) }.orEmpty()
        check(steps.all { it.questUserId == assignment?.id })
        return QuestExecution(quest, assignment?.id, assignment?.status, api.activities(token, questId).sortedBy { it.order }.map { activity ->
            val saved = steps.singleOrNull { it.activityId == activity.id }
            ActivityStep(activity.id, saved?.id, activity.description, activity.type, saved?.progress == 100.0)
        })
    }
}
