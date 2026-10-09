package pe.greenminds.ecomind.quests.infrastructure.local
import pe.greenminds.ecomind.quests.domain.repositories.QuestExecutionRepository
import pe.greenminds.ecomind.quests.domain.model.QuestExecution
import javax.inject.Inject
class LocalQuestExecutionRepository @Inject constructor(private val quests: LocalQuestRepository) : QuestExecutionRepository {
    override suspend fun load(questId: Long) = quests.getQuest(questId).map { QuestExecution(it, null, null, emptyList()) }
    override suspend fun start(questId: Long): Result<QuestExecution> = unavailable()
    override suspend fun check(questId: Long, assignmentId: Long, done: Boolean): Result<QuestExecution> = unavailable()
    override suspend fun finish(questId: Long): Result<QuestExecution> = unavailable()
    private fun unavailable() = Result.failure<QuestExecution>(IllegalStateException("Connect to the service to record completion"))
}
