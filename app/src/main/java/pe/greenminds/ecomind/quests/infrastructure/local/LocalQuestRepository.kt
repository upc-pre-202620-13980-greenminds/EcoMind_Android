package pe.greenminds.ecomind.quests.infrastructure.local

import kotlinx.coroutines.delay
import pe.greenminds.ecomind.quests.domain.entity.Quest
import pe.greenminds.ecomind.quests.domain.valueobject.QuestCategory
import pe.greenminds.ecomind.quests.domain.valueobject.QuestType
import pe.greenminds.ecomind.quests.domain.repositories.QuestRepository
import pe.greenminds.ecomind.quests.infrastructure.remote.QuestDto
import pe.greenminds.ecomind.quests.infrastructure.remote.toDomain
import pe.greenminds.ecomind.shared.infrastructure.remote.ErrorDto
import pe.greenminds.ecomind.shared.infrastructure.remote.toException
import javax.inject.Inject

// Demo implementation used until the web services are deployed
class LocalQuestRepository @Inject constructor() : QuestRepository {

    companion object {
        private const val SIMULATED_DELAY_MILLIS = 400L
    }

    // Sample quests with the shape of the responses of the web services
    private val quests = listOf(
        quest(
            id = 1L,
            title = "Time to save up energy!",
            description = "We will turn off the lights and unplug the devices we are not " +
                "using, learning how small gestures can protect our planet.",
            category = "ENERGY",
            type = "COLLABORATIVE",
            theme = "COLLABORATIVE",
            ecopoints = 50,
            time = 60
        ),
        quest(
            id = 2L,
            title = "Turn off unnecessary lights",
            description = "Check the rooms at home and turn off any lights you are not using.",
            category = "ENERGY",
            type = "ACTIVITIES",
            theme = "CHECKBOX",
            ecopoints = 30,
            time = 10
        ),
        quest(
            id = 3L,
            title = "Unplug before bed",
            description = "Before going to sleep, unplug the chargers nobody is using.",
            category = "ENERGY",
            type = "ACTIVITIES",
            theme = "CHECKBOX",
            ecopoints = 10,
            time = 5,
            assignedDate = "2026-10-08"
        ),
        quest(
            id = 4L,
            title = "Shorter showers",
            description = "Take a shower of five minutes or less and close the tap while you soap up.",
            category = "WATER",
            type = "ACTIVITIES",
            theme = "CHECKBOX",
            ecopoints = 20,
            time = 15
        ),
        quest(
            id = 5L,
            title = "Catch the drops",
            description = "Play to catch every drop before it is wasted and learn where water goes.",
            category = "WATER",
            type = "MINIGAME",
            theme = "MINIGAME",
            ecopoints = 15,
            time = 5,
            minigameId = 1L
        ),
        quest(
            id = 6L,
            title = "Sort your waste",
            description = "Separate paper, plastic and organic waste at home for one day.",
            category = "RECYCLE",
            type = "ACTIVITIES",
            theme = "CHECKBOX",
            ecopoints = 25,
            time = 20
        ),
        quest(
            id = 7L,
            title = "Recycling race",
            description = "Team up with your friends and collect as many recyclable items as you can.",
            category = "RECYCLE",
            type = "COLLABORATIVE",
            theme = "COLLABORATIVE",
            ecopoints = 40,
            time = 30
        ),
        quest(
            id = 8L,
            title = "Reuse a bottle today",
            description = "Find a new use for a plastic bottle instead of throwing it away.",
            category = "RECYCLE",
            type = "DAILY_QUEST",
            theme = "CHECKBOX",
            ecopoints = 10,
            time = 10,
            assignedDate = "2026-10-08"
        ),
        quest(
            id = 9L,
            title = "Daily Quest",
            description = "Complete the eco action of the day.",
            category = "ENERGY",
            type = "DAILY_QUEST",
            theme = "CHECKBOX",
            ecopoints = 10,
            time = 10,
            assignedDate = "2026-10-08"
        ),
        quest(
            id = 10L,
            title = "Save up water",
            description = "Reduce the water your family uses at home during the week.",
            category = "ENERGY",
            type = "ACTIVITIES",
            theme = "CHECKBOX",
            ecopoints = 40,
            time = 30
        ),
        quest(
            id = 11L,
            title = "Build a boat together!",
            description = "Build a small boat with recycled materials as a family.",
            category = "ENERGY",
            type = "ACTIVITIES",
            theme = "COLLABORATIVE",
            ecopoints = 50,
            time = 60
        )
    )

    // Equivalent to GET /quests/{questId}
    override suspend fun getQuest(questId: Long): Result<Quest> {
        val dto = quests.find { it.id == questId }
            ?: return Result.failure(
                ErrorDto(code = "QUEST_NOT_FOUND", message = "The quest was not found.").toException()
            )
        return Result.success(dto.toDomain())
    }

    // Equivalent to GET /quests. Only published quests are presented to the user.
    override suspend fun getQuests(): Result<List<Quest>> {
        // Simulates the time a request to the web services would take
        delay(SIMULATED_DELAY_MILLIS)
        // Same mapper the remote implementation will use
        return Result.success(quests.map { it.toDomain() })
    }

    override suspend fun searchQuests(
        query: String,
        category: QuestCategory?,
        questType: QuestType?
    ): Result<List<Quest>> {
        delay(SIMULATED_DELAY_MILLIS)
        val matches = quests.filter { dto ->
            dto.title.contains(query, ignoreCase = true) &&
                (category == null || dto.category == category.name) &&
                (questType == null || dto.type == questType.name)
        }
        return Result.success(matches.map { it.toDomain() })
    }

    private fun quest(
        id: Long,
        title: String,
        description: String,
        category: String,
        type: String,
        theme: String,
        ecopoints: Int,
        time: Int,
        minigameId: Long? = null,
        assignedDate: String? = null
    ): QuestDto {
        return QuestDto(
            id = id,
            versionGroupId = id,
            versionNumber = 1,
            publicationStatus = "PUBLISHED",
            minigameId = minigameId,
            title = title,
            description = description,
            category = category,
            type = type,
            gemReward = ecopoints,
            ecopoints = ecopoints,
            age = 8,
            time = time,
            theme = theme,
            assignedDate = assignedDate,
            image = null
        )
    }
}
