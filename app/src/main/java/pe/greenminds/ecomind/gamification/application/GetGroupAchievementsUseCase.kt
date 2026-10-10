package pe.greenminds.ecomind.gamification.application

import pe.greenminds.ecomind.gamification.domain.model.AchievementCollection
import pe.greenminds.ecomind.gamification.domain.model.AchievementGroup
import pe.greenminds.ecomind.gamification.domain.repositories.ProgressRepository
import javax.inject.Inject

class GetGroupAchievementsUseCase @Inject constructor(private val repository: ProgressRepository) {

    suspend operator fun invoke(group: AchievementGroup): Result<AchievementCollection> = repository.getGroupAchievements(group)
}
