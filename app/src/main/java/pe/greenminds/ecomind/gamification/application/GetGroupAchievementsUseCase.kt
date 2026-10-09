package pe.greenminds.ecomind.gamification.application
import pe.greenminds.ecomind.gamification.domain.repositories.ProgressRepository
import pe.greenminds.ecomind.gamification.domain.model.AchievementGroup
import javax.inject.Inject
class GetGroupAchievementsUseCase @Inject constructor(private val repository: ProgressRepository) {
    suspend operator fun invoke(group: AchievementGroup) = repository.getGroupAchievements(group)
}
