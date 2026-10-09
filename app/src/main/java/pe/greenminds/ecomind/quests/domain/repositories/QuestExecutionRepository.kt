package pe.greenminds.ecomind.quests.domain.repositories
import pe.greenminds.ecomind.quests.domain.model.QuestExecution
interface QuestExecutionRepository {
    suspend fun load(questId: Long): Result<QuestExecution>
    suspend fun start(questId: Long): Result<QuestExecution>
    suspend fun check(questId: Long, assignmentId: Long, done: Boolean): Result<QuestExecution>
    suspend fun finish(questId: Long): Result<QuestExecution>
}
