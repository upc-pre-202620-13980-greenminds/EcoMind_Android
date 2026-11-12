package pe.greenminds.ecomind.community.application.usecase

import javax.inject.Inject
import pe.greenminds.ecomind.community.domain.entity.Community
import pe.greenminds.ecomind.community.domain.entity.NewTopicCommunity
import pe.greenminds.ecomind.community.domain.repositories.CommunityDirectoryRepository

class CreateTopicCommunityUseCase @Inject constructor(
    private val repository: CommunityDirectoryRepository
) {
    suspend operator fun invoke(
        community: NewTopicCommunity
    ): Result<Community> = repository.createTopicCommunity(community)
}