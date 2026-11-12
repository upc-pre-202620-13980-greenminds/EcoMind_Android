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

class RemoteCommunityRepository @Inject constructor(
    private val api: CommunityApi,
    private val sessions: SessionRepository,
    @ApplicationContext private val context: Context
) : CommunityRepository {

    override suspend fun getEvents(communityId: Long) =
        execute({ api.getEvents(communityId) }) { list -> list.map { it.toDomain() } }

    override suspend fun getPosts(communityId: Long) =
        execute({ api.getPosts(communityId) }) { list -> list.map { it.toDomain() } }

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