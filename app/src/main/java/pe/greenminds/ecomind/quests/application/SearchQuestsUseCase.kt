package pe.greenminds.ecomind.quests.application

import pe.greenminds.ecomind.quests.domain.entity.Quest
import pe.greenminds.ecomind.quests.domain.repositories.QuestRepository
import pe.greenminds.ecomind.quests.domain.valueobject.QuestCategory
import pe.greenminds.ecomind.quests.domain.valueobject.QuestType
import javax.inject.Inject

class SearchQuestsUseCase @Inject constructor(
    private val repository: QuestRepository
) {
    suspend operator fun invoke(
        query: String,
        category: QuestCategory? = null,
        questType: QuestType? = null
    ): Result<List<Quest>> = repository.searchQuests(
        query = query.trim(),
        category = category,
        questType = questType
    )
}
