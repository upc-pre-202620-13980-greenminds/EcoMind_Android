package pe.greenminds.ecomind.quests.application

import kotlinx.serialization.Serializable
import kotlinx.coroutines.CancellationException
import pe.greenminds.ecomind.quests.domain.entity.Quest
import pe.greenminds.ecomind.quests.domain.repositories.QuestExecutionRepository
import pe.greenminds.ecomind.quests.domain.valueobject.*
import javax.inject.Inject

@Serializable
data class QuestFilters(
    val categories: Set<String> = emptySet(),
    val types: Set<String> = emptySet(),
    val ages: Set<String> = emptySet(),
    val times: Set<String> = emptySet()
)

class FilterQuestsUseCase @Inject constructor(private val execution: QuestExecutionRepository) {
    suspend operator fun invoke(quests: List<Quest>, filters: QuestFilters): Result<List<Quest>> = try {
        val result = quests.filter { quest ->
            (filters.categories.isEmpty() || quest.category.name in filters.categories) &&
                (filters.ages.isEmpty() || quest.targetAge == null || filters.ages.any { rangeMatches(it, quest.targetAge) }) &&
                (filters.times.isEmpty() || quest.estimatedMinutes?.let { time -> filters.times.any { rangeMatches(it, time) } } == true)
        }.filter { quest ->
            when {
                filters.types.isEmpty() -> true
                quest.type == QuestType.COLLABORATIVE || quest.theme == QuestTheme.COLLABORATIVE -> "COLLABORATIVE" in filters.types
                quest.type == QuestType.MINIGAME || quest.theme == QuestTheme.MINIGAME -> "MINIGAME" in filters.types
                filters.types.any { it == "CHECKBOX" || it == "WRITE" } ->
                    execution.activities(quest.id).any { it.type.name in filters.types }
                else -> false
            }
        }
        Result.success(result)
    } catch (e: CancellationException) { throw e }
    catch (e: Exception) { Result.failure(e) }
}

internal fun rangeMatches(range: String, value: Int): Boolean {
    val (from, to) = range.split(":").map(String::toInt)
    return value in from..to
}
