package pe.greenminds.ecomind.community.application.usecase

import javax.inject.Inject
import pe.greenminds.ecomind.community.domain.repositories.CommunityRepository

class GetCommunityGoalPanelUseCase @Inject constructor(private val repository: CommunityRepository) {
    suspend operator fun invoke(communityId: Long) = repository.getGoalPanel(communityId)
}