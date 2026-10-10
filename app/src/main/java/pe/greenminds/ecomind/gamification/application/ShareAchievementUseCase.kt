package pe.greenminds.ecomind.gamification.application

import pe.greenminds.ecomind.gamification.domain.model.AchievementShare
import pe.greenminds.ecomind.gamification.domain.repositories.ProgressRepository
import javax.inject.Inject

class ShareAchievementUseCase @Inject constructor(private val repository: ProgressRepository) {

    suspend operator fun invoke(awardId: String, communityId: Long): Result<AchievementShare> = repository.share(awardId, communityId)
}
