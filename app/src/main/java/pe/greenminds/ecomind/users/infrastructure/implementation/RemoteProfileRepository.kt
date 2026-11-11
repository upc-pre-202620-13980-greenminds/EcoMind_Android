package pe.greenminds.ecomind.users.infrastructure.implementation

import pe.greenminds.ecomind.shared.infrastructure.remote.apiResult
import pe.greenminds.ecomind.shared.infrastructure.remote.requireBody
import pe.greenminds.ecomind.users.domain.model.SocialRole
import pe.greenminds.ecomind.users.domain.model.UserProfile
import pe.greenminds.ecomind.users.domain.repositories.ProfileRepository
import pe.greenminds.ecomind.users.infrastructure.remote.UpdateUserProfileRequest
import pe.greenminds.ecomind.users.infrastructure.remote.UsersApi
import pe.greenminds.ecomind.users.infrastructure.remote.toDomain
import javax.inject.Inject

class RemoteProfileRepository @Inject constructor(
    private val api: UsersApi
) : ProfileRepository {

    companion object {
        // Set to true and run again to see the error state of the edit profile screen
        private const val SIMULATE_SAVE_FAILURE = false
    }

    override suspend fun getProfile(userId: Long): Result<UserProfile> = apiResult {
        api.getProfile(userId).requireBody().toDomain()
    }

    // The web services create the profile themselves when the account is verified
    override suspend fun createProfile(userId: Long, name: String, socialRole: SocialRole) = Unit

    // Gems are deducted by Monetization in the web services when an item is bought,
    // so there is no profile route to spend them from the app
    override suspend fun spendGems(userId: Long, amount: Int): Result<Int> {
        return Result.failure(
            UnsupportedOperationException("Gems are deducted by Monetization in the web services")
        )
    }

    override suspend fun updateName(userId: Long, name: String): Result<UserProfile> = apiResult {
        check(!SIMULATE_SAVE_FAILURE) { "Simulated save failure" }

        // The PUT replaces every field, so the current values are read again right before it.
        // This shortens, but does not remove, the time in which another screen could change them.
        val current = api.getProfile(userId).requireBody()
        val request = UpdateUserProfileRequest(
            name = name,
            streak = current.streak,
            lastStreakDate = current.lastStreakDate,
            ecopoints = current.ecopoints,
            gemBalance = current.gemBalance
        )
        api.updateProfile(userId, request)
            .requireBody()
            .toDomain()
    }
}
