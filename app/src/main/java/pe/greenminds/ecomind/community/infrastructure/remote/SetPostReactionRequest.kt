package pe.greenminds.ecomind.community.infrastructure.remote

import com.google.gson.annotations.SerializedName

data class SetPostReactionRequest(
    @SerializedName("post_id") val postId: Long,
    @SerializedName("reaction_type") val reactionType: String
)