package pe.greenminds.ecomind.gamification.interfaces.acl

import pe.greenminds.ecomind.gamification.application.GetGroupAchievementsUseCase
import pe.greenminds.ecomind.gamification.domain.model.AchievementGroup
import javax.inject.Inject

data class FamilyAchievement(val awardId: String, val name: String)

class GamificationContextFacade @Inject constructor(
    private val getGroupAchievements: GetGroupAchievementsUseCase
) {
    suspend fun getFamilyAchievements(familyId: Long): Result<List<FamilyAchievement>> =
        getGroupAchievements(AchievementGroup(familyId, "", "FAMILY")).map { collection ->
            collection.entries.mapNotNull { entry ->
                entry.award?.let { FamilyAchievement(it.id, entry.achievement.name) }
            }
        }
}
