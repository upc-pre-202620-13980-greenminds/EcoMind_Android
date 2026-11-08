package pe.greenminds.ecomind.users.infrastructure.implementation

import pe.greenminds.ecomind.shared.infrastructure.remote.apiResult
import pe.greenminds.ecomind.shared.infrastructure.remote.requireBody
import pe.greenminds.ecomind.users.domain.model.FamilyMembership
import pe.greenminds.ecomind.users.domain.model.FamilyRole
import pe.greenminds.ecomind.users.domain.repositories.FamilyMembershipRepository
import pe.greenminds.ecomind.users.infrastructure.remote.FamilyMembershipApi
import javax.inject.Inject

class RemoteFamilyMembershipRepository @Inject constructor(
    private val api: FamilyMembershipApi
) : FamilyMembershipRepository {
    override suspend fun getMembership(userId: Long): Result<FamilyMembership?> = apiResult {
        val memberships = api.getMemberships(userId).requireBody()
        check(memberships.all { it.userId == userId }) { "Unexpected family membership user" }
        check(memberships.size <= 1) { "The user has more than one family membership" }
        memberships.singleOrNull()?.let {
            FamilyMembership(it.id, it.familyId, it.userId, FamilyRole.valueOf(it.familyRole))
        }
    }
}
