package pe.greenminds.ecomind.community.domain.repositories

import pe.greenminds.ecomind.community.domain.entity.CommunityAchievement
import pe.greenminds.ecomind.community.domain.entity.CommunityEvent
import pe.greenminds.ecomind.community.domain.entity.CommunityGoal
import pe.greenminds.ecomind.community.domain.entity.CommunityPost
import pe.greenminds.ecomind.community.domain.entity.NewCommunityEvent
import pe.greenminds.ecomind.community.domain.entity.EventParticipation
import pe.greenminds.ecomind.community.domain.entity.EventChatMessage
import pe.greenminds.ecomind.community.domain.entity.EventChatPage

interface CommunityRepository {
    suspend fun getEvents(communityId: Long): Result<List<CommunityEvent>>
    suspend fun getPosts(communityId: Long): Result<List<CommunityPost>>
    suspend fun getAchievements(communityId: Long): Result<List<CommunityAchievement>>
    suspend fun getGoals(communityId: Long): Result<List<CommunityGoal>>
    suspend fun canCreateEvents(): Result<Boolean>

    suspend fun uploadEventImage(localUri: String): Result<String>

    suspend fun createEvent(event: NewCommunityEvent): Result<CommunityEvent>

    suspend fun getEventParticipation(communityId: Long?): Result<List<EventParticipation>>

    suspend fun registerForEvent(eventId: Long, familyId: Long?): Result<Unit>

    suspend fun leaveEvent(eventId: Long): Result<Unit>

    suspend fun cancelEventGroup(eventId: Long, registrationId: Long): Result<Unit>

    suspend fun getEventChatMessages(eventId: Long, afterId: Long): Result<EventChatPage>

    suspend fun sendEventChatMessage(eventId: Long, text: String, clientId: String): Result<EventChatMessage>

    suspend fun deleteEvent(eventId: Long): Result<Unit>
}