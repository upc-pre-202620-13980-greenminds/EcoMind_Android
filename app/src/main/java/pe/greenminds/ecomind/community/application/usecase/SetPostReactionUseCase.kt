package pe.greenminds.ecomind.community.application.usecase

import javax.inject.Inject
import pe.greenminds.ecomind.community.domain.repositories.CommunityRepository
import pe.greenminds.ecomind.community.domain.valueobjects.PostReactionType

class SetPostReactionUseCase @Inject constructor(private val repository: CommunityRepository) {
    suspend operator fun invoke(postId: Long, type: PostReactionType): Result<Unit> = repository.setPostReaction(postId, type)
}