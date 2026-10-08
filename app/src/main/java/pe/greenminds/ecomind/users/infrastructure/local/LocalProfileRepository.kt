package pe.greenminds.ecomind.users.infrastructure.local

import kotlinx.coroutines.delay
import pe.greenminds.ecomind.users.domain.model.SocialRole
import pe.greenminds.ecomind.users.domain.model.UserProfile
import pe.greenminds.ecomind.users.domain.repositories.ProfileRepository
import javax.inject.Inject

// Demo implementation used until the web services are deployed
class LocalProfileRepository @Inject constructor() : ProfileRepository {

    companion object {
        private const val SIMULATED_DELAY_MILLIS = 400L
    }

    override suspend fun getProfile(userId: Long): Result<UserProfile> {
        // Simulates the time a request to the web services would take
        delay(SIMULATED_DELAY_MILLIS)

        // Every user gets the same sample progress shown in the design
        val profile = UserProfile(
            id = userId,
            name = "Alex Green",
            socialRole = SocialRole.STUDENT,
            streak = 5,
            ecopoints = 16,
            gemBalance = 360
        )
        return Result.success(profile)
    }
}
