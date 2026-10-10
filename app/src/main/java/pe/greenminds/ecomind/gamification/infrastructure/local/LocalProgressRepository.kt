package pe.greenminds.ecomind.gamification.infrastructure.local

import kotlinx.coroutines.flow.first
import pe.greenminds.ecomind.gamification.domain.model.*
import pe.greenminds.ecomind.gamification.domain.repositories.ProgressRepository
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import javax.inject.Inject

class LocalProgressRepository @Inject constructor(private val sessions: SessionRepository) : ProgressRepository {
    override val isSimulated = true
    override suspend fun getOverview(): Result<GamificationOverview> {
        val session = sessions.getSession().first()
        if (session == null || session.isExpired(System.currentTimeMillis())) return Result.failure(AchievementSessionRequiredException())
        return Result.success(GamificationOverview(UserProgress(session.accountId, if (session.accountId == 1L) 16 else 0,
            0, 0, null, null), emptyList(), null, true))
    }
    override suspend fun getHistory(fromMillis: Long, toMillis: Long) = Result.success(emptyList<RewardHistory>())
    override suspend fun getGroupAchievements(group: AchievementGroup) = Result.success(AchievementCollection(emptyList(), true))
    override suspend fun getShare(awardId: String): Result<AchievementShare?> = Result.success(null)
    override suspend fun share(awardId: String, communityId: Long): Result<AchievementShare> =
        Result.failure(IllegalStateException("Sharing requires the connected service"))
}
