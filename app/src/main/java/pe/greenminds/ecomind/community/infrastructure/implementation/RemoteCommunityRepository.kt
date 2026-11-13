package pe.greenminds.ecomind.community.infrastructure.implementation

import com.google.gson.Gson
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import pe.greenminds.ecomind.community.domain.repositories.CommunityRepository
import pe.greenminds.ecomind.community.infrastructure.mapper.*
import pe.greenminds.ecomind.community.infrastructure.remote.CommunityApi
import pe.greenminds.ecomind.shared.infrastructure.remote.ErrorDto
import pe.greenminds.ecomind.shared.infrastructure.remote.toException
import retrofit2.HttpException
import retrofit2.Response
import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.community.domain.entity.CommunityEvent
import pe.greenminds.ecomind.community.domain.entity.NewCommunityEvent
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.community.domain.entity.EventChatMessage
import pe.greenminds.ecomind.community.domain.entity.EventChatPage
import pe.greenminds.ecomind.community.domain.valueobjects.EventChatUnavailableException
import pe.greenminds.ecomind.community.infrastructure.remote.RegisterForEventRequest
import pe.greenminds.ecomind.community.infrastructure.remote.SendEventChatMessageRequest
import pe.greenminds.ecomind.shared.infrastructure.remote.ApiException
import pe.greenminds.ecomind.community.domain.entity.CommunityPost
import pe.greenminds.ecomind.community.domain.entity.PostReactions
import pe.greenminds.ecomind.community.domain.valueobjects.PostReactionType
import pe.greenminds.ecomind.community.infrastructure.remote.PostAuthorDto
import pe.greenminds.ecomind.community.infrastructure.remote.SetPostReactionRequest
import pe.greenminds.ecomind.community.domain.entity.CommunityGoalPanel
import pe.greenminds.ecomind.community.infrastructure.remote.CreateCommunityGoalRequest

class RemoteCommunityRepository @Inject constructor(
    private val api: CommunityApi,
    private val sessions: SessionRepository,
    @ApplicationContext private val context: Context
) : CommunityRepository {

    override suspend fun getEvents(communityId: Long) =
        execute({ api.getEvents(communityId) }) { list -> list.map { it.toDomain() } }

    override suspend fun getPosts(communityId: Long): Result<List<CommunityPost>> {
        val posts = execute({ api.getPosts(communityId) }) { list ->
            list.map { it.toDomain() }
        }.getOrElse {
            return Result.failure(it)
        }

        val authors = mutableMapOf<Long, PostAuthorDto?>()

        posts.mapNotNull { it.authorId }.distinct().forEach { authorId ->
            authors[authorId] = execute({ api.getPostAuthor(authorId) }) {
                it
            }.getOrNull()
        }

        return Result.success(
            posts.map { post ->
                val author = authors[post.authorId]
                post.copy(
                    authorName = author?.name,
                    authorAvatar = author?.avatarImageReference
                )
            }.sortedWith(
                compareByDescending<CommunityPost> { it.createdAt }
                    .thenByDescending { it.id }
            )
        )
    }
    override suspend fun getAchievements(communityId: Long) =
        execute({ api.getAchievements(communityId) }) { list -> list.map { it.toDomain() } }

    override suspend fun getGoals(communityId:Long) =
        execute({ api.getCommunityGoals(communityId) }) { list -> list.map { it.toDomain() } }

    override suspend fun canCreateEvents(): Result<Boolean> = execute(
        request = {
            val session = sessions.getSession().first()
                ?: throw IllegalStateException(
                    context.getString(R.string.event_session_required)
                )

            if (session.isExpired(System.currentTimeMillis())) {
                throw IllegalStateException(
                    context.getString(R.string.event_session_required)
                )
            }

            api.getEventCreatorProfile(session.accountId)
        },
        transform = {
            it.socialRole.equals("PARENT", ignoreCase = true)
        }
    )

    override suspend fun createEvent(
        event: NewCommunityEvent
    ): Result<CommunityEvent> =
        execute({ api.createEvent(event.toRequest()) }) { it.toDomain() }

    override suspend fun uploadEventImage(
        localUri: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            val uri = Uri.parse(localUri)
            val mime = resolver.getType(uri)

            if (mime != "image/jpeg" && mime != "image/png") {
                throw IllegalArgumentException(
                    context.getString(R.string.event_image_type)
                )
            }

            val content = resolver.openInputStream(uri)?.use { input ->
                ByteArrayOutputStream().use { output ->
                    val buffer = ByteArray(8192)
                    var total = 0

                    while (true) {
                        val count = input.read(buffer)
                        if (count == -1) break

                        total += count

                        if (total > 5 * 1024 * 1024) {
                            throw IllegalArgumentException(
                                context.getString(R.string.event_image_size)
                            )
                        }

                        output.write(buffer, 0, count)
                    }

                    output.toByteArray()
                }
            } ?: throw IllegalArgumentException(
                context.getString(R.string.event_image_read)
            )

            if (content.isEmpty()) {
                throw IllegalArgumentException(
                    context.getString(R.string.event_image_read)
                )
            }

            val extension = if (mime == "image/png") "png" else "jpg"

            val part = MultipartBody.Part.createFormData(
                "file",
                "event.$extension",
                content.toRequestBody(mime.toMediaType())
            )

            execute({ api.uploadEventImage(part) }) { it.imageUrl }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun getEventParticipation(communityId: Long?) =
        execute(
            request = {
                if (communityId == null) api.getMyEvents()
                else api.getEventParticipation(communityId)
            },
            transform = { list -> list.map { it.toDomain() } }
        )

    override suspend fun registerForEvent(
        eventId: Long,
        familyId: Long?
    ): Result<Unit> = executeWithoutBody {
        api.registerForEvent(
            eventId,
            RegisterForEventRequest(
                registrationType = if (familyId == null) "INDIVIDUAL" else "FAMILY",
                familyId = familyId
            )
        )
    }

    override suspend fun leaveEvent(eventId: Long): Result<Unit> =
        executeWithoutBody { api.leaveEvent(eventId) }

    override suspend fun deleteEvent(eventId: Long): Result<Unit> =
        executeWithoutBody { api.deleteEvent(eventId) }

    override suspend fun cancelEventGroup(
        eventId: Long,
        registrationId: Long
    ): Result<Unit> = executeWithoutBody {
        api.cancelEventGroup(eventId, registrationId)
    }

    override suspend fun getEventChatMessages(
        eventId: Long,
        afterId: Long
    ): Result<EventChatPage> =
        execute({ api.getEventChatMessages(eventId, afterId) }) {
            it.toDomain()
        }.mapChatFailure()

    override suspend fun sendEventChatMessage(
        eventId: Long,
        text: String,
        clientId: String
    ): Result<EventChatMessage> =
        execute({
            api.sendEventChatMessage(
                eventId,
                SendEventChatMessageRequest(text, clientId)
            )
        }) {
            it.toDomain()
        }.mapChatFailure()

    override suspend fun getPostReactions(
        postId: Long
    ): Result<PostReactions> {
        var accountId: Long? = null

        return execute(
            request = {
                val session = sessions.getSession().first()
                    ?: throw IllegalStateException(
                        context.getString(R.string.news_session_required)
                    )

                if (session.isExpired(System.currentTimeMillis())) {
                    throw IllegalStateException(
                        context.getString(R.string.news_session_required)
                    )
                }

                accountId = session.accountId
                api.getPostReactions(postId)
            },
            transform = { reactions ->
                val counts = reactions
                    .groupingBy {
                        PostReactionType.fromValue(it.reactionType)
                    }
                    .eachCount()

                val ownReaction = reactions
                    .firstOrNull { it.userId == accountId }
                    ?.let { PostReactionType.fromValue(it.reactionType) }

                PostReactions(
                    counts = counts,
                    ownReaction = ownReaction
                )
            }
        )
    }

    override suspend fun setPostReaction(
        postId: Long,
        type: PostReactionType
    ): Result<Unit> =
        execute({
            api.setPostReaction(
                SetPostReactionRequest(
                    postId = postId,
                    reactionType = type.value
                )
            )
        }) {
            Unit
        }

    override suspend fun removePostReaction(
        postId: Long
    ): Result<Unit> =
        executeWithoutBody {
            api.removePostReaction(postId)
        }

    override suspend fun getGoalPanel(
        communityId: Long
    ): Result<CommunityGoalPanel> =
        execute({ api.getGoalPanel(communityId) }) { it.toDomain() }

    override suspend fun createGoal(
        communityId: Long,
        topic: String
    ): Result<Unit> =
        execute({
            api.createGoal(CreateCommunityGoalRequest(communityId, topic))
        }) {
            Unit
        }

    override suspend fun joinGoal(goalId: Long): Result<Unit> =
        execute({ api.joinGoal(goalId) }) {
            Unit
        }
    private fun <T> Result<T>.mapChatFailure(): Result<T> = fold(
        onSuccess = { Result.success(it) },
        onFailure = { exception ->
            val unavailable = when (exception) {
                is ApiException -> exception.code in setOf(
                    "EVENT_CHAT_FORBIDDEN",
                    "EVENT_CHAT_EXPIRED",
                    "EVENT_NOT_FOUND"
                )
                is HttpException -> exception.code() == 403 || exception.code() == 404
                else -> false
            }

            Result.failure(
                if (unavailable) EventChatUnavailableException(exception.message)
                else exception
            )
        }
    )

    private suspend fun executeWithoutBody(
        request: suspend () -> Response<Unit>
    ): Result<Unit> = try {
        val response = request()

        if (response.isSuccessful) {
            Result.success(Unit)
        } else {
            val error = try {
                response.errorBody()?.use {
                    Gson().fromJson(it.string(), ErrorDto::class.java)
                }
            } catch (_: Exception) {
                null
            }

            Result.failure(error?.toException() ?: HttpException(response))
        }
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }
    private suspend fun <Dto : Any, Model> execute(
        request: suspend () -> Response<Dto>,
        transform: (Dto) -> Model
    ): Result<Model> = try {
        val response = request()

        if (!response.isSuccessful) {
            val error = try {
                response.errorBody()
                    ?.use { Gson().fromJson(it.string(), ErrorDto::class.java) }
            } catch (_: Exception) {
                null
            }
            Result.failure(error?.toException() ?: HttpException(response))
        } else {
            val body = response.body()
                ?: throw IllegalStateException("Community response is empty")
            Result.success(transform(body))
        }
    } catch (exception: CancellationException) {
        throw exception
    } catch (exception: Exception) {
        Result.failure(exception)
    }
}