package pe.greenminds.ecomind.users.infrastructure.remote

import pe.greenminds.ecomind.shared.infrastructure.remote.RemoteAccess
import pe.greenminds.ecomind.users.domain.repositories.FriendRepository
import javax.inject.Inject

class RemoteFriendRepository @Inject constructor(private val api: UsersApi, private val access: RemoteAccess) : FriendRepository {
    override suspend fun getFriendships(userId: Long) = access.authenticated { session ->
        check(session.accountId == userId)
        api.friends(session, userId).filter { it.requesterId == userId || it.receiverId == userId }.map { it.toDomain() }
    }
}
