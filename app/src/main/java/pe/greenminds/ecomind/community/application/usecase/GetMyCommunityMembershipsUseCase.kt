package pe.greenminds.ecomind.community.application.usecase

import javax.inject.Inject
import pe.greenminds.ecomind.community.domain.entity.CommunityMembership
import pe.greenminds.ecomind.community.domain.repositories.CommunityDirectoryRepository

class GetMyCommunityMembershipsUseCase @Inject constructor(
    private val repository: CommunityDirectoryRepository
) {
    suspend operator fun invoke(): Result<List<CommunityMembership>> = repository.getMyMemberships()
}