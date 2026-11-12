package pe.greenminds.ecomind.community.infrastructure.remote

import com.google.gson.annotations.SerializedName

data class PostReactionDto(
    val id: Long,
    @SerializedName("post_id") val postId: Long,
    @SerializedName("user_id") val userId: Long,
    @SerializedName("reaction_type") val reactionType: String
)