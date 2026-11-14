package pe.greenminds.ecomind.community.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.community.application.usecase.CanCreateCommunityEventsUseCase
import pe.greenminds.ecomind.community.application.usecase.CreateCommunityEventUseCase
import pe.greenminds.ecomind.community.application.usecase.UploadEventImageUseCase
import pe.greenminds.ecomind.community.domain.entity.Community
import pe.greenminds.ecomind.community.domain.entity.NewCommunityEvent
import pe.greenminds.ecomind.community.presentation.states.CreateEventUiState
import pe.greenminds.ecomind.community.presentation.states.EventFormField

@HiltViewModel
class CreateEventViewModel @Inject constructor(
    private val canCreateEvents: CanCreateCommunityEventsUseCase,
    private val uploadImage: UploadEventImageUseCase,
    private val createEvent: CreateCommunityEventUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CreateEventUiState())
    val state = _state.asStateFlow()

    private var permissionJob: Job? = null

    fun refreshPermission() {
        if (_state.value.isSubmitting || permissionJob?.isActive == true) return

        permissionJob = viewModelScope.launch {
            _state.update {
                it.copy(isCheckingPermission = true, permissionError = null)
            }

            canCreateEvents()
                .onSuccess { allowed ->
                    _state.update {
                        it.copy(
                            canCreate = allowed,
                            isCheckingPermission = false
                        )
                    }
                }
                .onFailure { error ->
                    _state.update {
                        it.copy(
                            canCreate = false,
                            isCheckingPermission = false,
                            permissionError = error.message.orEmpty()
                        )
                    }
                }
        }
    }

    fun start(community: Community) {
        if (!_state.value.canCreate || _state.value.isSubmitting) return

        _state.value = CreateEventUiState(
            community = community,
            canCreate = true
        )
    }

    fun close() {
        if (_state.value.isSubmitting) return

        _state.update {
            it.copy(community = null, createdEventId = null)
        }
    }

    fun change(field: EventFormField, value: String) {
        if (_state.value.isSubmitting) return

        _state.update { current ->
            val updated = when (field) {
                EventFormField.NAME -> current.copy(name = value)
                EventFormField.CAPACITY -> current.copy(capacity = value)
                EventFormField.DESCRIPTION -> current.copy(description = value)
                EventFormField.DATE -> current.copy(date = value)
                EventFormField.TIME -> current.copy(time = value)
                EventFormField.ADDRESS -> current.copy(address = value)
                EventFormField.LATITUDE -> current.copy(latitude = value)
                EventFormField.LONGITUDE -> current.copy(longitude = value)
            }

            updated.copy(errorMessage = null, errorRes = null)
        }
    }

    fun selectImage(uri: String?) {
        if (_state.value.isSubmitting) return

        _state.update {
            it.copy(
                imageUri = uri,
                uploadedImageUrl = null,
                errorMessage = null,
                errorRes = null
            )
        }
    }

    fun selectPoint(latitude: Double, longitude: Double) {
        if (_state.value.isSubmitting) return
        if (!latitude.isFinite() || !longitude.isFinite()) return
        if (latitude !in -90.0..90.0 || longitude !in -180.0..180.0) return

        _state.update {
            it.copy(
                latitude = String.format(Locale.US, "%.6f", latitude),
                longitude = String.format(Locale.US, "%.6f", longitude),
                errorMessage = null,
                errorRes = null
            )
        }
    }

    fun submit() {
        val current = _state.value

        if (current.isSubmitting || current.isCheckingPermission) return

        if (!current.canCreate) {
            fail(R.string.event_parent_required)
            return
        }

        val community = current.community ?: return

        if (current.name.isBlank() || current.name.trim().length > 255) {
            fail(R.string.event_invalid_name)
            return
        }

        val capacity = current.capacity.toIntOrNull()

        if (capacity == null || capacity < 1) {
            fail(R.string.event_invalid_capacity)
            return
        }

        val date = runCatching {
            LocalDate.parse(current.date)
        }.getOrNull()

        val time = runCatching {
            LocalTime.parse(current.time)
        }.getOrNull()

        if (date == null || time == null) {
            fail(R.string.event_invalid_datetime)
            return
        }

        val latitude = current.latitude.replace(',', '.').toDoubleOrNull()
        val longitude = current.longitude.replace(',', '.').toDoubleOrNull()

        if (
            latitude == null || longitude == null ||
            !latitude.isFinite() || !longitude.isFinite() ||
            latitude !in -90.0..90.0 || longitude !in -180.0..180.0
        ) {
            fail(R.string.event_invalid_coordinates)
            return
        }

        val event = NewCommunityEvent(
            communityId = community.id,
            name = current.name.trim(),
            description = current.description.trim().ifEmpty { null },
            date = date.toString(),
            startTime = time.format(
                DateTimeFormatter.ofPattern("HH:mm:ss", Locale.US)
            ),
            location = current.address.trim().ifEmpty { null },
            latitude = latitude,
            longitude = longitude,
            capacity = capacity
        )

        _state.update {
            it.copy(
                isSubmitting = true,
                errorMessage = null,
                errorRes = null
            )
        }

        viewModelScope.launch {
            try {
                var imageUrl = current.uploadedImageUrl

                if (current.imageUri != null && imageUrl == null) {
                    imageUrl = uploadImage(current.imageUri).getOrThrow()

                    _state.update {
                        it.copy(uploadedImageUrl = imageUrl)
                    }
                }

                val created = createEvent(
                    event.copy(imageUrl = imageUrl)
                ).getOrThrow()

                _state.update {
                    it.copy(createdEventId = created.id)
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _state.update {
                    it.copy(
                        errorMessage = exception.message,
                        errorRes = if (exception.message.isNullOrBlank()) {
                            R.string.event_failed
                        } else {
                            null
                        }
                    )
                }
            } finally {
                _state.update {
                    it.copy(isSubmitting = false)
                }
            }
        }
    }

    private fun fail(message: Int) {
        _state.update {
            it.copy(errorRes = message, errorMessage = null)
        }
    }
}