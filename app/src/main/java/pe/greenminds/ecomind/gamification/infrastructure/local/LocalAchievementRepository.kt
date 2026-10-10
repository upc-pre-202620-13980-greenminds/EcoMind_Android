package pe.greenminds.ecomind.gamification.infrastructure.local

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import pe.greenminds.ecomind.gamification.domain.model.Achievement
import pe.greenminds.ecomind.gamification.domain.model.AchievementAward
import pe.greenminds.ecomind.gamification.domain.model.AchievementSessionRequiredException
import pe.greenminds.ecomind.gamification.domain.repositories.AchievementRepository
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import javax.inject.Inject

// Read-only fixtures, clearly identified as demo by the collection and detail screens.
// The app never grants a medal by opening a screen or by reaching a locally computed threshold.
class LocalAchievementRepository @Inject constructor(
    private val sessions: SessionRepository
) : AchievementRepository {
    override val isSimulated = true

    override suspend fun getCatalog(): Result<List<Achievement>> {
        delay(250)
        return Result.success(listOf(
            Achievement(SECTION_ID, "DEMO_SECTION", "Section completed", "Recognition for completing a learning section.", "INDIVIDUAL", "ECOPOINTS", 10, true),
            Achievement(ACTIVITY_ID, "DEMO_ACTIVITY", "Important activity", "Recognition for completing an important activity.", "INDIVIDUAL", "ECOPOINTS", 15, true),
            Achievement(STREAK_ID, "DEMO_STREAK", "Steady practice", "Recognition for continuity in eligible activities.", "INDIVIDUAL", "LONGEST_STREAK", 7, true)
        ))
    }

    override suspend fun getMyAwards(): Result<List<AchievementAward>> {
        delay(250)
        val session = sessions.getSession().first()
            ?: return Result.failure(AchievementSessionRequiredException())
        // The seeded student has the two medals drawn in Figma; other demo accounts start empty.
        val awards = if (session.accountId == 1L) listOf(
            AchievementAward("10000000-0000-0000-0000-000000000001", SECTION_ID, "INDIVIDUAL", 1L, "20000000-0000-0000-0000-000000000001", "2026-10-07T15:00:00Z", null),
            AchievementAward("10000000-0000-0000-0000-000000000002", ACTIVITY_ID, "INDIVIDUAL", 1L, "20000000-0000-0000-0000-000000000002", "2026-10-08T15:00:00Z", null)
        ) else emptyList()
        return Result.success(awards)
    }

    companion object {
        const val SECTION_ID = "00000000-0000-0000-0000-000000000001"
        const val ACTIVITY_ID = "00000000-0000-0000-0000-000000000002"
        const val STREAK_ID = "00000000-0000-0000-0000-000000000003"
    }
}
