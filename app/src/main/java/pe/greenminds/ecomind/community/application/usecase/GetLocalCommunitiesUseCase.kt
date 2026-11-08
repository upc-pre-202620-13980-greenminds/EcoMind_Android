package pe.greenminds.ecomind.community.application.usecase

import javax.inject.Inject
import pe.greenminds.ecomind.community.domain.entity.Community
import pe.greenminds.ecomind.community.domain.repositories.CommunityDirectoryRepository

class GetLocalCommunitiesUseCase @Inject constructor(
    private val repository: CommunityDirectoryRepository
) {
    suspend operator fun invoke(locality: String? = null): Result<List<Community>> =
        repository.getLocalCommunities(locality?.trim()?.takeIf(String::isNotEmpty))
}