package pe.greenminds.ecomind.users.infrastructure.remote

import pe.greenminds.ecomind.users.domain.model.Family
import pe.greenminds.ecomind.users.domain.model.FamilyMembership
import pe.greenminds.ecomind.users.domain.model.FamilyRelationship
import pe.greenminds.ecomind.users.domain.model.FamilyRole
import pe.greenminds.ecomind.users.domain.model.Friendship
import pe.greenminds.ecomind.users.domain.model.FriendshipStatus
import pe.greenminds.ecomind.users.domain.model.SocialRole
import pe.greenminds.ecomind.users.domain.model.UserProfile

// The commitment is not part of the response yet, so it arrives as a separate value
fun UserProfileDto.toDomain(commitment: String? = null): UserProfile {
    return UserProfile(
        id = id,
        name = name,
        socialRole = SocialRole.valueOf(socialRole),
        streak = streak,
        ecopoints = ecopoints,
        gemBalance = gemBalance,
        commitment = commitment
    )
}

fun FriendDto.toDomain(): Friendship {
    return Friendship(
        id = id,
        requesterId = requesterId,
        receiverId = receiverId,
        status = FriendshipStatus.valueOf(status)
    )
}

// Relationship and progress are not part of the response yet, so they arrive as separate values
fun FamilyMemberDto.toDomain(
    relationship: FamilyRelationship? = null,
    activitiesCompleted: Int? = null,
    activitiesTotal: Int? = null
): FamilyMembership {
    return FamilyMembership(
        id = id,
        familyId = familyId,
        userId = userId,
        familyRole = FamilyRole.valueOf(familyRole),
        relationship = relationship,
        activitiesCompleted = activitiesCompleted,
        activitiesTotal = activitiesTotal
    )
}

fun FamilyDto.toDomain(memberships: List<FamilyMembership>): Family {
    return Family(
        id = id,
        name = name,
        commitment = commitment,
        memberships = memberships
    )
}
