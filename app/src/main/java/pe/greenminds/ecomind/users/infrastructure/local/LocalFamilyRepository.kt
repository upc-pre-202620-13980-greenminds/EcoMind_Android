package pe.greenminds.ecomind.users.infrastructure.local

import kotlinx.coroutines.delay
import pe.greenminds.ecomind.users.domain.model.Family
import pe.greenminds.ecomind.users.domain.model.WeeklyReport
import pe.greenminds.ecomind.users.domain.repositories.FamilyRepository
import pe.greenminds.ecomind.users.infrastructure.remote.toDomain
import javax.inject.Inject

// Demo implementation used until the web services are deployed
class LocalFamilyRepository @Inject constructor(
    private val dataSource: LocalUsersDataSource
) : FamilyRepository {

    companion object {
        private const val SIMULATED_DELAY_MILLIS = 400L
    }

    override suspend fun getFamilyOf(userId: Long): Result<Family?> {
        delay(SIMULATED_DELAY_MILLIS)

        // Equivalent to GET /family_user?user_id={id}: only the membership of the user
        val membership = dataSource.families
            .flatMap { it.members }
            .find { it.userId == userId }
            ?: return Result.success(null)

        // Equivalent to GET /family: there is no route to read a single family
        val familyDto = dataSource.families.find { it.id == membership.familyId }
            ?: return Result.success(null)

        val memberships = familyDto.members.map { memberDto ->
            val details = ProvisionalProfileData.memberDetailsOf(memberDto.userId)
            memberDto.toDomain(
                relationship = details?.relationship,
                activitiesCompleted = details?.activitiesCompleted,
                activitiesTotal = details?.activitiesTotal
            )
        }
        return Result.success(familyDto.toDomain(memberships))
    }

    override suspend fun getWeeklyReport(familyId: Long): Result<WeeklyReport?> {
        return Result.success(ProvisionalProfileData.weeklyReportOf(familyId))
    }
}
