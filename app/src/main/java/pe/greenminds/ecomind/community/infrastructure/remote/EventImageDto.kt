package pe.greenminds.ecomind.community.infrastructure.remote

import com.google.gson.annotations.SerializedName

data class EventImageDto(
    @SerializedName("public_id") val publicId: String,
    @SerializedName("image_url") val imageUrl: String
)