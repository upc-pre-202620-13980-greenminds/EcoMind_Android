package pe.greenminds.ecomind.community.domain.valueobjects

enum class LocalityFailure {
    PERMISSION_DENIED,
    LOCATION_DISABLED,
    LOCATION_UNAVAILABLE,
    GEOCODING_UNAVAILABLE
}

class LocalityException(
    val reason: LocalityFailure
) : Exception(reason.name)