package pe.greenminds.ecomind.community.application.usecase

import javax.inject.Inject
import pe.greenminds.ecomind.community.domain.repositories.CommunityRepository

class JoinCommunityGoalUseCase @Inject constructor(private val repository: CommunityRepository) {
    suspend operator fun invoke(goalId: Long) = repository.joinGoal(goalId)
}