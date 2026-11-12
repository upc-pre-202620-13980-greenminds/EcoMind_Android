package pe.greenminds.ecomind.community.application.usecase

import javax.inject.Inject
import pe.greenminds.ecomind.community.domain.entity.CommunityMembership
import pe.greenminds.ecomind.community.domain.repositories.CommunityDirectoryRepository

class JoinCommunityUseCase @Inject constructor(
    private val repository: CommunityDirectoryRepository
) {
    suspend operator fun invoke(communityId: Long): Result<CommunityMembership> {
        require(communityId > 0) { "Community ID must be positive" }
        return repository.joinCommunity(communityId)
    }
}