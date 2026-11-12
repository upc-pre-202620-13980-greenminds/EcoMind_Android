package pe.greenminds.ecomind.community.application.usecase

import javax.inject.Inject
import pe.greenminds.ecomind.community.domain.entity.CommunityAchievement
import pe.greenminds.ecomind.community.domain.repositories.CommunityRepository

class GetCommunityAchievementsUseCase @Inject constructor(private val repository: CommunityRepository) {
    suspend operator fun invoke(communityId: Long): Result<List<CommunityAchievement>> = repository.getAchievements(communityId)
}