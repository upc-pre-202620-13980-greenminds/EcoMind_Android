package pe.greenminds.ecomind.users.domain.repositories

import pe.greenminds.ecomind.users.domain.model.Family
import pe.greenminds.ecomind.users.domain.model.WeeklyReport

interface FamilyRepository {

    // Null when the user does not belong to a family
    suspend fun getFamilyOf(userId: Long): Result<Family?>

    // Null when the family has no report for the week
    suspend fun getWeeklyReport(familyId: Long): Result<WeeklyReport?>
}
