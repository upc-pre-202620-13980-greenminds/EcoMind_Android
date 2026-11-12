package pe.greenminds.ecomind.community.presentation.states

import pe.greenminds.ecomind.community.domain.entity.Community

data class CreateEventUiState(
    val community: Community? = null,
    val canCreate: Boolean = false,
    val isCheckingPermission: Boolean = false,
    val permissionError: String? = null,
    val name: String = "",
    val capacity: String = "",
    val description: String = "",
    val date: String = "",
    val time: String = "",
    val address: String = "",
    val latitude: String = "",
    val longitude: String = "",
    val imageUri: String? = null,
    val uploadedImageUrl: String? = null,
    val isSubmitting: Boolean = false,
    val createdEventId: Long? = null,
    val errorMessage: String? = null,
    val errorRes: Int? = null
)

enum class EventFormField {
    NAME, CAPACITY, DESCRIPTION, DATE, TIME, ADDRESS, LATITUDE, LONGITUDE
}