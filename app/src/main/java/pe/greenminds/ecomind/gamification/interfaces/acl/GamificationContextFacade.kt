package pe.greenminds.ecomind.gamification.interfaces.acl

import pe.greenminds.ecomind.gamification.application.GetAchievementsUseCase
import pe.greenminds.ecomind.gamification.application.GetGroupAchievementsUseCase
import pe.greenminds.ecomind.gamification.domain.model.AchievementGroup
import javax.inject.Inject

data class FamilyAchievement(val awardId: String, val name: String)

data class MyAchievement(val awardId: String, val achievementId: String, val name: String)

class GamificationContextFacade @Inject constructor(
    private val getGroupAchievements: GetGroupAchievementsUseCase,
    private val getAchievements: GetAchievementsUseCase
) {
    // Medals already earned by the signed in user, used by the profile
    suspend fun getMyAchievements(): Result<List<MyAchievement>> =
        getAchievements().map { collection ->
            collection.earned.mapNotNull { entry ->
                entry.award?.let { MyAchievement(it.id, entry.achievement.id, entry.achievement.name) }
            }
        }

    suspend fun getFamilyAchievements(familyId: Long): Result<List<FamilyAchievement>> =
        getGroupAchievements(AchievementGroup(familyId, "", "FAMILY")).map { collection ->
            collection.entries.mapNotNull { entry ->
                entry.award?.let { FamilyAchievement(it.id, entry.achievement.name) }
            }
        }
}
