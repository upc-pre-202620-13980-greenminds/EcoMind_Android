package pe.greenminds.ecomind.users.domain.repositories

import pe.greenminds.ecomind.users.domain.model.FamilyMembership

interface FamilyMembershipRepository {
    suspend fun getMembership(userId: Long): Result<FamilyMembership?>
}
