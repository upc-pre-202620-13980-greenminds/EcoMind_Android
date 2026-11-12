package pe.greenminds.ecomind.community.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.community.application.usecase.CanCreateCommunityEventsUseCase
import pe.greenminds.ecomind.community.application.usecase.CreateTopicCommunityUseCase
import pe.greenminds.ecomind.community.application.usecase.UploadEventImageUseCase
import pe.greenminds.ecomind.community.domain.entity.NewTopicCommunity
import pe.greenminds.ecomind.community.domain.valueobjects.CommunityFormField
import pe.greenminds.ecomind.community.presentation.states.CreateCommunityUiState

@HiltViewModel
class CreateCommunityViewModel @Inject constructor(
    private val checkParent: CanCreateCommunityEventsUseCase,
    private val uploadImage: UploadEventImageUseCase,
    private val createCommunity: CreateTopicCommunityUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CreateCommunityUiState())
    val state = _state.asStateFlow()

    private var permissionJob: Job? = null

    fun refreshPermission() {
        if (_state.value.isSubmitting || permissionJob?.isActive == true) return

        _state.update {
            it.copy(
                isCheckingPermission = true,
                permissionError = null
            )
        }

        permissionJob = viewModelScope.launch {
            checkParent().fold(
                onSuccess = { allowed ->
                    _state.update {
                        it.copy(
                            canCreate = allowed,
                            isCheckingPermission = false
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            canCreate = false,
                            isCheckingPermission = false,
                            permissionError = error.message.orEmpty()
                        )
                    }
                }
            )
        }
    }

    fun open() {
        val current = _state.value
        if (!current.canCreate || current.isCheckingPermission || current.isSubmitting) return

        _state.value = CreateCommunityUiState(
            isOpen = true,
            canCreate = true,
            isCheckingPermission = false
        )
    }

    fun close() {
        if (_state.value.isSubmitting) return

        _state.update {
            it.copy(
                isOpen = false,
                createdCommunity = null
            )
        }
    }

    fun change(field: CommunityFormField, value: String) {
        if (_state.value.isSubmitting) return

        _state.update { current ->
            val updated = when (field) {
                CommunityFormField.NAME -> current.copy(name = value)
                CommunityFormField.DESCRIPTION -> current.copy(description = value)
                CommunityFormField.TOPIC -> current.copy(topic = value)
                CommunityFormField.LIMIT -> current.copy(
                    limit = value.filter { it in '0'..'9' }.take(10)
                )
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

    fun submit() {
        val current = _state.value

        if (
            !current.isOpen ||
            current.isSubmitting ||
            current.isCheckingPermission ||
            !current.canCreate
        ) return

        val memberLimit = current.limit.toIntOrNull()

        if (
            current.name.isBlank() ||
            current.topic.isBlank() ||
            memberLimit == null ||
            memberLimit < 1
        ) {
            _state.update {
                it.copy(errorRes = R.string.community_form_validation)
            }
            return
        }

        _state.update {
            it.copy(
                isSubmitting = true,
                errorMessage = null,
                errorRes = null
            )
        }

        viewModelScope.launch {
            try {
                var iconUrl = current.uploadedImageUrl

                if (current.imageUri != null && iconUrl == null) {
                    iconUrl = uploadImage(current.imageUri).getOrThrow()

                    _state.update {
                        it.copy(uploadedImageUrl = iconUrl)
                    }
                }

                val community = createCommunity(
                    NewTopicCommunity(
                        name = current.name.trim(),
                        description = current.description.trim()
                            .takeIf { it.isNotEmpty() },
                        topic = current.topic.trim(),
                        memberLimit = memberLimit,
                        iconUrl = iconUrl
                    )
                ).getOrThrow()

                _state.update {
                    it.copy(
                        isSubmitting = false,
                        createdCommunity = community
                    )
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _state.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = exception.message?.takeIf { message ->
                            message.isNotBlank()
                        },
                        errorRes = R.string.community_form_error
                    )
                }
            }
        }
    }
}