package pe.greenminds.ecomind.community.application.usecase

import javax.inject.Inject
import pe.greenminds.ecomind.community.domain.repositories.CommunityRepository

class RemovePostReactionUseCase @Inject constructor(private val repository: CommunityRepository) {
    suspend operator fun invoke(postId: Long): Result<Unit> = repository.removePostReaction(postId)
}