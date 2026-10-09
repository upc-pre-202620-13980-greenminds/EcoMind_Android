package pe.greenminds.ecomind.gamification.application
import pe.greenminds.ecomind.gamification.domain.repositories.ProgressRepository
import pe.greenminds.ecomind.gamification.domain.model.RankingPeriod
import pe.greenminds.ecomind.gamification.domain.services.RankingPeriodRange
import javax.inject.Inject
class GetRewardHistoryUseCase @Inject constructor(private val repository: ProgressRepository) {
    suspend operator fun invoke(period: RankingPeriod) : Result<List<pe.greenminds.ecomind.gamification.domain.model.RewardHistory>> {
        val now = System.currentTimeMillis()
        return repository.getHistory(RankingPeriodRange.startOf(period, now) ?: 0L, now)
    }
}
