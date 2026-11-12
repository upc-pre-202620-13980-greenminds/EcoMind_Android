package pe.greenminds.ecomind.community.infrastructure.remote

import com.google.gson.annotations.SerializedName

data class CommunityPostDto(
    val id: Long,
    @SerializedName("community_id") val communityId: Long,
    @SerializedName("author_id") val authorId: Long?,
    val content: String,
    @SerializedName("post_type") val postType: String?,
    @SerializedName("image_url") val imageUrl: String?,
    @SerializedName("related_event_id") val relatedEventId: Long?,
    val likes: Long
)