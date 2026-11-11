package pe.greenminds.ecomind.users.application

import kotlinx.coroutines.flow.first
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.users.domain.model.FriendRelation
import pe.greenminds.ecomind.users.domain.model.UserSearchResult
import pe.greenminds.ecomind.users.domain.repositories.FriendRepository
import pe.greenminds.ecomind.users.domain.repositories.ProfileRepository
import javax.inject.Inject

class SearchUsersUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
    private val profileRepository: ProfileRepository,
    private val friendRepository: FriendRepository
) {

    // An empty query lists every user, as the design shows before typing
    suspend operator fun invoke(query: String): Result<List<UserSearchResult>> {
        val session = sessionRepository.getSession().first()
            ?: return Result.failure(IllegalStateException("There is no stored session"))
        val userId = session.accountId

        // The web services have no search route, so the profiles are filtered here
        val profiles = profileRepository.getProfiles().getOrElse { return Result.failure(it) }
        val friendships = friendRepository.getFriendships(userId).getOrElse { return Result.failure(it) }
        val relations = friendships.associate { friendship ->
            friendship.otherUserId(userId) to friendship.relationFor(userId)
        }

        val text = query.trim()
        val results = profiles
            .filter { it.id != userId }
            .filter { text.isEmpty() || it.name.contains(text, ignoreCase = true) }
            .map { profile ->
                UserSearchResult(
                    userId = profile.id,
                    name = profile.name,
                    socialRole = profile.socialRole,
                    relation = relations[profile.id] ?: FriendRelation.NONE
                )
            }
        return Result.success(results)
    }
}
