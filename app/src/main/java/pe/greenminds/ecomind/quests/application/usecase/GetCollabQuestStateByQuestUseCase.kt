package pe.greenminds.ecomind.quests.application.usecase

import pe.greenminds.ecomind.quests.domain.model.CollabQuestState
import pe.greenminds.ecomind.quests.domain.repositories.CollaborativeQuestRepository
import javax.inject.Inject

class GetCollabQuestStateByQuestUseCase @Inject constructor(
    private val repository: CollaborativeQuestRepository
) {
    suspend operator fun invoke(
        questId: Long
    ): Result<CollabQuestState> =
        repository.getStateByQuest(questId)
}