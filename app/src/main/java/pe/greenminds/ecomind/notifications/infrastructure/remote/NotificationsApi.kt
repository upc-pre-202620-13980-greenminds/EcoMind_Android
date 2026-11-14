package pe.greenminds.ecomind.notifications.infrastructure.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.PATCH
import retrofit2.http.Path

// Routes of TS-016. POST /notification exists too, but the app does not create notifications.
interface NotificationsApi {

    // The texts come in the language of Accept-Language
    @GET("notification")
    suspend fun getNotifications(
        @Header("Accept-Language") language: String
    ): Response<List<NotificationDto>>

    @GET("notification/unread-count")
    suspend fun getUnreadCount(): Response<UnreadCountDto>

    @PATCH("notification/{id}/read")
    suspend fun markAsRead(
        @Path("id") notificationId: Long,
        @Header("Accept-Language") language: String
    ): Response<NotificationDto>

    // Answers 204 without a body
    @PATCH("notification/read-all")
    suspend fun markAllAsRead(): Response<Unit>
}
