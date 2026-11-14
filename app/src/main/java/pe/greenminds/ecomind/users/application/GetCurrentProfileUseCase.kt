package pe.greenminds.ecomind.users.application

import pe.greenminds.ecomind.users.domain.model.UserProfile
import pe.greenminds.ecomind.users.domain.repositories.CommitmentRepository
import pe.greenminds.ecomind.users.infrastructure.remote.UsersApi
import pe.greenminds.ecomind.users.infrastructure.remote.toDomain
import pe.greenminds.ecomind.shared.infrastructure.offline.OfflineSnapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import pe.greenminds.ecomind.shared.infrastructure.remote.requireBody
import javax.inject.Inject

class GetCurrentProfileUseCase @Inject constructor(
    private val snapshots: OfflineSnapshots,
    private val api: UsersApi,
    private val commitmentRepository: CommitmentRepository
) {

    suspend operator fun invoke(): Result<UserProfile> {
        return snapshots.fetch("profile", UserProfile::class.java) { session ->
            try {
                Result.success(api.getProfile(session.accountId, session).requireBody().toDomain())
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Result.failure(error)
            }
        }.map { withCommitment(it) }
    }

    suspend fun observeSaved() = snapshots.session()?.let { session ->
        snapshots.observe<UserProfile>(session, "profile", UserProfile::class.java)
            .filterNotNull()
            .map { withCommitment(it) }
    }

    private suspend fun withCommitment(profile: UserProfile): UserProfile =
        profile.copy(commitment = commitmentRepository.getCommitment(profile.id).getOrNull())
}
