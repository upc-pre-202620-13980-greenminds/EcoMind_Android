package pe.greenminds.ecomind.quests.infrastructure.local

import kotlinx.coroutines.delay
import pe.greenminds.ecomind.quests.domain.model.FamilyPlan
import pe.greenminds.ecomind.quests.domain.model.QuestStatus
import pe.greenminds.ecomind.quests.domain.model.QuestUser
import pe.greenminds.ecomind.quests.domain.repositories.QuestProgressRepository
import pe.greenminds.ecomind.quests.infrastructure.remote.FamilyPlanDto
import pe.greenminds.ecomind.quests.infrastructure.remote.FamilyPlanItemDto
import pe.greenminds.ecomind.quests.infrastructure.remote.QuestUserDto
import pe.greenminds.ecomind.quests.infrastructure.remote.toDomain
import javax.inject.Inject

// Demo implementation used until the web services are deployed.
// The data is fixed and the same for every user: it shows the main screen of the design.
class LocalQuestProgressRepository @Inject constructor() : QuestProgressRepository {

    companion object {
        private const val SIMULATED_DELAY_MILLIS = 400L
    }

    // The quest ids are the ones of LocalQuestRepository, where the titles come from
    private val questUsers = listOf(
        questUser(id = 1L, questId = 9L, status = "IN_PROGRESS", progress = 70.0),
        questUser(id = 2L, questId = 1L, status = "IN_PROGRESS", progress = 30.0),
        questUser(id = 3L, questId = 10L, status = "COMPLETED", progress = 100.0)
    )

    private val activeFamilyPlan = FamilyPlanDto(
        id = 1L,
        familyId = 1L,
        ownerUserId = 2L,
        status = "ACTIVE",
        progress = 50.0,
        items = listOf(
            FamilyPlanItemDto(id = 1L, questId = 10L, collaborativeSessionId = null, progress = 70.0),
            FamilyPlanItemDto(id = 2L, questId = 11L, collaborativeSessionId = null, progress = 30.0)
        )
    )

    // Equivalent to GET /quest-users/user/{userId}/status/{status}
    override suspend fun getQuestUsers(userId: Long, status: QuestStatus): Result<List<QuestUser>> {
        // Simulates the time a request to the web services would take
        delay(SIMULATED_DELAY_MILLIS)

        val dtos = questUsers.filter { it.status == status.name }
        // Same mapper the remote implementation will use
        return Result.success(dtos.map { it.toDomain() })
    }

    // Equivalent to GET /family-plans/active?familyId={id}
    override suspend fun getActiveFamilyPlan(userId: Long): Result<FamilyPlan?> {
        return Result.success(activeFamilyPlan.toDomain())
    }

    private fun questUser(id: Long, questId: Long, status: String, progress: Double): QuestUserDto {
        return QuestUserDto(
            id = id,
            userId = 1L,
            questId = questId,
            status = status,
            progress = progress,
            endDate = null,
            collaborativeSessionId = null
        )
    }
}
