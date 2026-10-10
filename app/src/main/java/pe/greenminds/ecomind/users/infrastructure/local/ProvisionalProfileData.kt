package pe.greenminds.ecomind.users.infrastructure.local

import pe.greenminds.ecomind.users.domain.model.FamilyRelationship
import pe.greenminds.ecomind.users.domain.model.WeeklyReport

// PROVISIONAL: everything in this file is shown in the design but is missing in the web services.
// When the routes exist, these values move to the DTOs and this file is deleted.
object ProvisionalProfileData {

    data class MemberDetails(
        val relationship: FamilyRelationship,
        val activitiesCompleted: Int,
        val activitiesTotal: Int
    )

    // Personal commitment of a user: no field in GET /user/{id}
    private val commitments = mapOf(
        LocalUsersDataSource.STUDENT_ID to
            "Build small sustainable habits at home, one day at a time."
    )

    // Relationship and progress of each member: GET /family_user only returns PARENT or CHILD
    private val memberDetails = mapOf(
        LocalUsersDataSource.PARENT_ID to MemberDetails(FamilyRelationship.FATHER, 4, 5),
        LocalUsersDataSource.STUDENT_ID to MemberDetails(FamilyRelationship.DAUGHTER, 2, 5),
        3L to MemberDetails(FamilyRelationship.DAUGHTER, 3, 5),
        4L to MemberDetails(FamilyRelationship.SON, 2, 4)
    )

    // Weekly report of a family: there is no route for it
    private val weeklyReports = mapOf(
        LocalUsersDataSource.FAMILY_ID to WeeklyReport(
            questsCompleted = 3,
            questsTotal = 5,
            achievementsEarned = 1
        )
    )

    fun commitmentOf(userId: Long): String? = commitments[userId]

    fun memberDetailsOf(userId: Long): MemberDetails? = memberDetails[userId]

    fun weeklyReportOf(familyId: Long): WeeklyReport? = weeklyReports[familyId]
}
