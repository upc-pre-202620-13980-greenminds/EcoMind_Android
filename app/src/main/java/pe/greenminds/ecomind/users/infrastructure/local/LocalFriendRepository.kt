package pe.greenminds.ecomind.users.infrastructure.local

import kotlinx.coroutines.delay
import pe.greenminds.ecomind.users.domain.model.Friendship
import pe.greenminds.ecomind.users.domain.repositories.FriendRepository
import pe.greenminds.ecomind.users.infrastructure.remote.toDomain
import javax.inject.Inject

// Demo implementation used until the web services are deployed
class LocalFriendRepository @Inject constructor(
    private val dataSource: LocalUsersDataSource
) : FriendRepository {

    companion object {
        private const val SIMULATED_DELAY_MILLIS = 400L
    }

    // Equivalent to GET /friend?user_id={id}
    override suspend fun getFriendships(userId: Long): Result<List<Friendship>> {
        delay(SIMULATED_DELAY_MILLIS)

        val dtos = dataSource.friends.filter { dto ->
            dto.requesterId == userId || dto.receiverId == userId
        }
        return Result.success(dtos.map { it.toDomain() })
    }
}
