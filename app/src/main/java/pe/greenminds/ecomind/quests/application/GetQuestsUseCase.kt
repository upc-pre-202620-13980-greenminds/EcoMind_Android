package pe.greenminds.ecomind.quests.application

import pe.greenminds.ecomind.quests.domain.entity.Quest
import pe.greenminds.ecomind.quests.domain.repositories.QuestRepository
import javax.inject.Inject

class GetQuestsUseCase @Inject constructor(
    private val repository: QuestRepository
) {
    suspend operator fun invoke(): Result<List<Quest>> = repository.getQuests()
}
