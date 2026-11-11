package pe.greenminds.ecomind.users.infrastructure.implementation

import pe.greenminds.ecomind.shared.infrastructure.remote.apiResult
import pe.greenminds.ecomind.shared.infrastructure.remote.requireBody
import pe.greenminds.ecomind.users.domain.model.Family
import pe.greenminds.ecomind.users.domain.model.WeeklyReport
import pe.greenminds.ecomind.users.domain.repositories.FamilyRepository
import pe.greenminds.ecomind.users.infrastructure.remote.UsersApi
import pe.greenminds.ecomind.users.infrastructure.remote.toDomain
import javax.inject.Inject

class RemoteFamilyRepository @Inject constructor(
    private val api: UsersApi
) : FamilyRepository {

    override suspend fun getFamilyOf(userId: Long): Result<Family?> = apiResult {
        // A user belongs to one family at most; an empty list means they have none (frame 16)
        val membership = api.getFamilyMemberships(userId).requireBody().firstOrNull()
        if (membership == null) {
            null
        } else {
            // There is no route to read a single family, so it is looked for among all of them
            api.getFamilies().requireBody()
                .find { it.id == membership.familyId }
                ?.let { familyDto -> familyDto.toDomain(familyDto.members.map { it.toDomain() }) }
        }
    }

    // The weekly report belongs to the parents (HU-021) and has no route in the web services
    override suspend fun getWeeklyReport(familyId: Long): Result<WeeklyReport?> = Result.success(null)
}
