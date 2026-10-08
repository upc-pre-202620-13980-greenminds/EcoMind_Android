package pe.greenminds.ecomind.quests.application

import pe.greenminds.ecomind.quests.domain.model.Quest
import pe.greenminds.ecomind.quests.domain.model.QuestFilter
import pe.greenminds.ecomind.quests.domain.repositories.QuestRepository
import javax.inject.Inject

class SearchQuestsUseCase @Inject constructor(private val repository: QuestRepository) {

    suspend operator fun invoke(filter: QuestFilter): Result<List<Quest>> {
        // Spaces around the text are not part of the search
        return repository.searchQuests(filter.copy(title = filter.title.trim()))
    }
}
