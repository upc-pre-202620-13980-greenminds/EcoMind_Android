package pe.greenminds.ecomind.users.application

import pe.greenminds.ecomind.users.domain.model.FamilyMember
import pe.greenminds.ecomind.users.domain.model.FamilyOverview
import pe.greenminds.ecomind.users.domain.model.FamilyRole
import pe.greenminds.ecomind.users.domain.repositories.FamilyRepository
import pe.greenminds.ecomind.users.domain.repositories.ProfileRepository
import javax.inject.Inject

class GetFamilyUseCase @Inject constructor(
    private val familyRepository: FamilyRepository,
    private val profileRepository: ProfileRepository
) {

    // The result holds null when the user does not belong to a family
    suspend operator fun invoke(userId: Long): Result<FamilyOverview?> {
        val family = familyRepository.getFamilyOf(userId)
            .getOrElse { return Result.failure(it) }
            ?: return Result.success(null)

        // A membership only has ids, so the name and role come from the profile of each member
        val members = family.memberships.mapNotNull { membership ->
            profileRepository.getProfile(membership.userId).getOrNull()?.let { profile ->
                FamilyMember(
                    userId = profile.id,
                    name = profile.name,
                    socialRole = profile.socialRole,
                    familyRole = membership.familyRole,
                    relationship = membership.relationship,
                    activitiesCompleted = membership.activitiesCompleted,
                    activitiesTotal = membership.activitiesTotal,
                    isCurrentUser = membership.userId == userId
                )
            }
        }

        val isManagedByCurrentUser = family.memberships.any { membership ->
            membership.userId == userId && membership.familyRole == FamilyRole.PARENT
        }

        val overview = FamilyOverview(
            id = family.id,
            name = family.name,
            commitment = family.commitment,
            // The current user goes first, as in the design
            members = members.sortedByDescending { it.isCurrentUser },
            isManagedByCurrentUser = isManagedByCurrentUser,
            weeklyReport = familyRepository.getWeeklyReport(family.id).getOrNull()
        )
        return Result.success(overview)
    }
}
