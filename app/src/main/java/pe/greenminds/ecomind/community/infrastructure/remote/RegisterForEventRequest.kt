package pe.greenminds.ecomind.community.infrastructure.remote

import com.google.gson.annotations.SerializedName

data class RegisterForEventRequest(
    @SerializedName("registration_type") val registrationType: String,
    @SerializedName("family_id") val familyId: Long?
)