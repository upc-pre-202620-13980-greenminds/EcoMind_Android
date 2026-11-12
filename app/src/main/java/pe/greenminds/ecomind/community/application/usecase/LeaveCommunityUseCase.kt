package pe.greenminds.ecomind.community.application.usecase

import javax.inject.Inject
import pe.greenminds.ecomind.community.domain.repositories.CommunityDirectoryRepository

class LeaveCommunityUseCase @Inject constructor(
    private val repository: CommunityDirectoryRepository
) {
    suspend operator fun invoke(communityId: Long): Result<Unit> {
        require(communityId > 0) { "Community ID must be positive" }
        return repository.leaveCommunity(communityId)
    }
}