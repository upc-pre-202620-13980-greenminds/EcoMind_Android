package pe.greenminds.ecomind.users.infrastructure.remote

import pe.greenminds.ecomind.shared.infrastructure.remote.RemoteAccess
import pe.greenminds.ecomind.users.domain.model.WeeklyReport
import pe.greenminds.ecomind.users.domain.repositories.FamilyRepository
import javax.inject.Inject

class RemoteFamilyRepository @Inject constructor(private val api: UsersApi, private val access: RemoteAccess) : FamilyRepository {
    override suspend fun getFamilyOf(userId: Long) = access.authenticated { session, token ->
        check(session.accountId == userId)
        val memberships = api.membership(token, userId).filter { it.userId == userId }
        check(memberships.map { it.familyId }.distinct().size <= 1)
        memberships.firstOrNull()?.let { member ->
            val family = api.families(token).first { it.id == member.familyId }
            family.toDomain(family.members.map { it.toDomain() })
        }
    }
    override suspend fun getWeeklyReport(familyId: Long): Result<WeeklyReport?> = Result.success(null)
}
