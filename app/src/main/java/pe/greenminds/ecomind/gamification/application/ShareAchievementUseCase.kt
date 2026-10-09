package pe.greenminds.ecomind.gamification.application
import pe.greenminds.ecomind.gamification.domain.repositories.ProgressRepository
import javax.inject.Inject
class ShareAchievementUseCase @Inject constructor(private val repository: ProgressRepository) {
    suspend fun status(awardId: String) = repository.getShare(awardId)
    suspend operator fun invoke(awardId: String, communityId: Long) = repository.share(awardId, communityId)
}
