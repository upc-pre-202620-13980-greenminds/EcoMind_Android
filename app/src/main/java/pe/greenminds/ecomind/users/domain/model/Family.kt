package pe.greenminds.ecomind.users.domain.model

enum class FamilyRole {
    PARENT,
    CHILD
}

// Provisional: the web services only know PARENT or CHILD
enum class FamilyRelationship {
    MOTHER,
    FATHER,
    DAUGHTER,
    SON
}

// Membership of a user in a family, as the web services store it
data class FamilyMembership(
    val id: Long,
    val familyId: Long,
    val userId: Long,
    val familyRole: FamilyRole,
    // Provisional: not returned by the web services yet
    val relationship: FamilyRelationship? = null,
    val activitiesCompleted: Int? = null,
    val activitiesTotal: Int? = null
)

data class Family(
    val id: Long,
    val name: String,
    val commitment: String,
    val memberships: List<FamilyMembership>
)

// Provisional: there is no weekly report in the web services yet
data class WeeklyReport(
    val questsCompleted: Int,
    val questsTotal: Int,
    val achievementsEarned: Int
)

// A member with the data of their profile, ready to be listed
data class FamilyMember(
    val userId: Long,
    val name: String,
    val socialRole: SocialRole,
    val familyRole: FamilyRole,
    val relationship: FamilyRelationship?,
    val activitiesCompleted: Int?,
    val activitiesTotal: Int?,
    val isCurrentUser: Boolean
)

data class FamilyOverview(
    val id: Long,
    val name: String,
    val commitment: String,
    val members: List<FamilyMember>,
    // Parents manage the family and can open its reports
    val isManagedByCurrentUser: Boolean,
    val weeklyReport: WeeklyReport?
)
