package pe.greenminds.ecomind.quests

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import pe.greenminds.ecomind.quests.application.*
import pe.greenminds.ecomind.quests.domain.entity.*
import pe.greenminds.ecomind.quests.domain.repositories.*
import pe.greenminds.ecomind.quests.domain.valueobject.*

class FilterQuestsUseCaseTest {
    private val energy = Quest(1, 1, 1, QuestPublicationStatus.PUBLISHED, null, "Save energy", "Lights",
        QuestCategory.ENERGY, QuestType.ACTIVITIES, QuestReward(0, 50), 10, 30, QuestTheme.CHECKBOX, null, null)
    private val collaborative = energy.copy(id = 2, type = QuestType.COLLABORATIVE, category = QuestCategory.WATER)
    private val write = energy.copy(id = 3)
    private val repository = object : QuestExecutionRepository {
        override suspend fun find(questId: Long): QuestUser? = error("unused")
        override suspend fun start(questId: Long): QuestUser = error("unused")
        override suspend fun get(id: Long): QuestUser = error("unused")
        override suspend fun activities(questId: Long) = listOf(Activity(questId, questId, "Activity", 1,
            if (questId == 3L) ActivityType.WRITE else ActivityType.CHECKBOX, emptyMap(), null))
        override suspend fun activityUsers(questUserId: Long): List<ActivityUser> = error("unused")
        override suspend fun check(activityUserId: Long, checked: Boolean): ActivityUser = error("unused")
        override suspend fun finish(questUserId: Long): QuestUser = error("unused")
    }
    @Test fun combinesGroupsAndIncludesRangeBoundaries() = runBlocking {
        val result = FilterQuestsUseCase(repository)(listOf(energy, collaborative, write), QuestFilters(
            categories = setOf("ENERGY"), types = setOf("CHECKBOX"), ages = setOf("10:13"), times = setOf("16:30"))).getOrThrow()
        assertEquals(listOf(energy), result)
    }
    @Test fun collaborativeTypeOverridesCheckboxTheme() = runBlocking {
        val result = FilterQuestsUseCase(repository)(listOf(energy, collaborative), QuestFilters(types = setOf("COLLABORATIVE"))).getOrThrow()
        assertEquals(listOf(collaborative), result)
    }
    @Test fun writeUsesActivityTypeAndClearedFiltersReturnEverything() = runBlocking {
        val quests = listOf(energy, collaborative, write)
        val useCase = FilterQuestsUseCase(repository)
        assertEquals(listOf(write), useCase(quests, QuestFilters(types = setOf("WRITE"))).getOrThrow())
        assertEquals(quests, useCase(quests, QuestFilters()).getOrThrow())
    }
}
