package pe.greenminds.ecomind.gamification.application

import pe.greenminds.ecomind.gamification.domain.model.GamificationOverview
import pe.greenminds.ecomind.gamification.domain.repositories.ProgressRepository
import javax.inject.Inject

class GetGamificationOverviewUseCase @Inject constructor(private val repository: ProgressRepository) {

    suspend operator fun invoke(): Result<GamificationOverview> = repository.getOverview()
}
