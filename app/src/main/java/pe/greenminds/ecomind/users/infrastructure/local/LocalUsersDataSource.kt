package pe.greenminds.ecomind.users.infrastructure.local

import pe.greenminds.ecomind.users.infrastructure.remote.FamilyDto
import pe.greenminds.ecomind.users.infrastructure.remote.FamilyMemberDto
import pe.greenminds.ecomind.users.infrastructure.remote.UserProfileDto
import javax.inject.Inject
import javax.inject.Singleton

// Sample data with the shape of the responses of the web services.
// It is a singleton because the profiles created at sign up only live in memory.
@Singleton
class LocalUsersDataSource @Inject constructor() {

    companion object {
        // Ids 1 and 2 are the demo accounts of IAM (student and parent)
        const val STUDENT_ID = 1L
        const val PARENT_ID = 2L
        const val FAMILY_ID = 1L
    }

    val profiles = mutableListOf(
        profile(STUDENT_ID, "Camila Torres", "STUDENT"),
        profile(PARENT_ID, "Robin Green", "PARENT"),
        profile(3L, "Luna Rivera", "STUDENT"),
        profile(4L, "Sal Torres", "STUDENT"),
        profile(5L, "Diego Ramos", "STUDENT"),
        profile(6L, "Valeria Chávez", "PARENT"),
        profile(7L, "Mateo Vargas", "PARENT")
    )

    val families = listOf(
        FamilyDto(
            id = FAMILY_ID,
            name = "Green Home",
            commitment = "Use less water together, every day.",
            members = listOf(
                FamilyMemberDto(id = 1L, familyId = FAMILY_ID, userId = PARENT_ID, familyRole = "PARENT"),
                FamilyMemberDto(id = 2L, familyId = FAMILY_ID, userId = STUDENT_ID, familyRole = "CHILD"),
                FamilyMemberDto(id = 3L, familyId = FAMILY_ID, userId = 3L, familyRole = "CHILD"),
                FamilyMemberDto(id = 4L, familyId = FAMILY_ID, userId = 4L, familyRole = "CHILD")
            )
        )
    )

    fun addProfile(userId: Long, name: String, socialRole: String) {
        if (profiles.none { it.id == userId }) {
            profiles.add(profile(userId, name, socialRole, streak = 0, ecopoints = 0, gemBalance = 0))
        }
    }

    private fun profile(
        id: Long,
        name: String,
        socialRole: String,
        streak: Int = 2,
        ecopoints: Int = 16,
        gemBalance: Int = 360
    ): UserProfileDto {
        return UserProfileDto(
            id = id,
            name = name,
            socialRole = socialRole,
            streak = streak,
            lastStreakDate = null,
            ecopoints = ecopoints,
            gemBalance = gemBalance,
            equippedCosmeticId = null
        )
    }
}
