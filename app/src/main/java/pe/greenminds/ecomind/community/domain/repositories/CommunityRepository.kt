package pe.greenminds.ecomind.community.domain.repositories

import pe.greenminds.ecomind.community.domain.entity.CommunityAchievement
import pe.greenminds.ecomind.community.domain.entity.CommunityEvent
import pe.greenminds.ecomind.community.domain.entity.CommunityGoal
import pe.greenminds.ecomind.community.domain.entity.CommunityPost
import pe.greenminds.ecomind.community.domain.entity.NewCommunityEvent

interface CommunityRepository {
    suspend fun getEvents(communityId: Long): Result<List<CommunityEvent>>
    suspend fun getPosts(communityId: Long): Result<List<CommunityPost>>
    suspend fun getAchievements(communityId: Long): Result<List<CommunityAchievement>>
    suspend fun getGoals(communityId: Long): Result<List<CommunityGoal>>
    suspend fun canCreateEvents(): Result<Boolean>

    suspend fun uploadEventImage(localUri: String): Result<String>

    suspend fun createEvent(event: NewCommunityEvent): Result<CommunityEvent>
}