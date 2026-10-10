package pe.greenminds.ecomind.gamification.application

import pe.greenminds.ecomind.gamification.domain.model.AchievementDetail
import pe.greenminds.ecomind.gamification.domain.model.AchievementNotFoundException
import javax.inject.Inject

class GetAchievementByIdUseCase @Inject constructor(
    private val getAchievements: GetAchievementsUseCase
) {
    suspend operator fun invoke(id: String): Result<AchievementDetail> {
        // Reuse the collection's session and ownership checks for this query.
        val collection = getAchievements().getOrElse { return Result.failure(it) }
        val entry = collection.entries.find { it.achievement.id == id }
            ?: return Result.failure(AchievementNotFoundException())
        return Result.success(AchievementDetail(entry, collection.isSimulated))
    }
}
