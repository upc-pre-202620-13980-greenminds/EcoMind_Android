package pe.greenminds.ecomind.users.infrastructure.local

import kotlinx.coroutines.delay
import pe.greenminds.ecomind.shared.infrastructure.remote.ErrorDto
import pe.greenminds.ecomind.shared.infrastructure.remote.toException
import pe.greenminds.ecomind.users.domain.model.SocialRole
import pe.greenminds.ecomind.users.domain.model.UserProfile
import pe.greenminds.ecomind.users.domain.repositories.ProfileRepository
import pe.greenminds.ecomind.users.infrastructure.remote.toDomain
import javax.inject.Inject

// Demo implementation used until the web services are deployed
class LocalProfileRepository @Inject constructor(
    private val dataSource: LocalUsersDataSource
) : ProfileRepository {

    companion object {
        private const val SIMULATED_DELAY_MILLIS = 200L

        // Set to true and run again to see the error state of the profile
        private const val SIMULATE_FAILURE = false
    }

    override suspend fun getProfile(userId: Long): Result<UserProfile> {
        // Simulates the time a request to the web services would take
        delay(SIMULATED_DELAY_MILLIS)

        val dto = dataSource.profiles.find { it.id == userId }
        if (SIMULATE_FAILURE || dto == null) {
            val error = ErrorDto(
                code = "USER_PROFILE_NOT_FOUND",
                message = "The user profile was not found."
            )
            return Result.failure(error.toException())
        }

        // Same mapper the remote implementation will use
        return Result.success(dto.toDomain(ProvisionalProfileData.commitmentOf(userId)))
    }

    override suspend fun createProfile(userId: Long, name: String, socialRole: SocialRole) {
        dataSource.addProfile(userId, name, socialRole.name)
    }
}
