package pe.greenminds.ecomind.quests.application
import pe.greenminds.ecomind.quests.domain.repositories.QuestExecutionRepository
import javax.inject.Inject
class QuestExecutionUseCase @Inject constructor(private val repository: QuestExecutionRepository) {
    suspend fun load(id: Long) = repository.load(id)
    suspend fun start(id: Long) = repository.start(id)
    suspend fun check(id: Long, assignmentId: Long, done: Boolean) = repository.check(id, assignmentId, done)
    suspend fun finish(id: Long) = repository.finish(id)
}
