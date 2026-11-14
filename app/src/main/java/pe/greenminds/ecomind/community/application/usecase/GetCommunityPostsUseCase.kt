package pe.greenminds.ecomind.community.application.usecase

import javax.inject.Inject
import pe.greenminds.ecomind.community.domain.entity.CommunityPost
import pe.greenminds.ecomind.community.domain.repositories.CommunityRepository

class GetCommunityPostsUseCase @Inject constructor(private val repository: CommunityRepository) {
    suspend operator fun invoke(communityId: Long): Result<List<CommunityPost>> = repository.getPosts(communityId)
}