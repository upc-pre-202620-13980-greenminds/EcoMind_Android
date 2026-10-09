package pe.greenminds.ecomind.gamification.application

import kotlinx.coroutines.flow.first
import pe.greenminds.ecomind.gamification.domain.model.AchievementCollection
import pe.greenminds.ecomind.gamification.domain.model.AchievementEntry
import pe.greenminds.ecomind.gamification.domain.model.AchievementSessionRequiredException
import pe.greenminds.ecomind.gamification.domain.repositories.AchievementRepository
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import javax.inject.Inject

class GetAchievementsUseCase @Inject constructor(
    private val repository: AchievementRepository,
    private val sessions: SessionRepository
) {
    suspend operator fun invoke(): Result<AchievementCollection> {
        val session = sessions.getSession().first()
        if (session == null || session.expiresAtMillis <= System.currentTimeMillis()) {
            return Result.failure(AchievementSessionRequiredException())
        }
        val catalog = repository.getCatalog().getOrElse { return Result.failure(it) }
        val awards = repository.getMyAwards().getOrElse { return Result.failure(it) }
        // Only the authenticated person's individual awards belong to this collection.
        // Repeated events cannot create duplicate rows. Historical inactive awards remain visible.
        val ownAwards = awards.filter { it.scope == "INDIVIDUAL" && it.beneficiaryId == session.accountId }
            .associateBy { it.achievementId }
        val knownIds = catalog.map { it.id }.toSet()
        if (ownAwards.keys.any { it !in knownIds }) {
            return Result.failure(IllegalStateException("An awarded achievement definition is unavailable"))
        }
        val currentSession = sessions.getSession().first()
        if (currentSession == null || currentSession.accountId != session.accountId || currentSession.isExpired(System.currentTimeMillis())) {
            return Result.failure(AchievementSessionRequiredException())
        }
        val entries = catalog.distinctBy { it.id }
            .filter { it.scope == "INDIVIDUAL" && (it.active || ownAwards.containsKey(it.id)) }
            .map { AchievementEntry(it, ownAwards[it.id]) }
        return Result.success(AchievementCollection(entries, repository.isSimulated))
    }
}
