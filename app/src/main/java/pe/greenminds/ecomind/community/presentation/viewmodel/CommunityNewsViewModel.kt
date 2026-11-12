package pe.greenminds.ecomind.community.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.community.application.usecase.GetCommunityPostsUseCase
import pe.greenminds.ecomind.community.application.usecase.GetPostReactionsUseCase
import pe.greenminds.ecomind.community.application.usecase.RemovePostReactionUseCase
import pe.greenminds.ecomind.community.application.usecase.SetPostReactionUseCase
import pe.greenminds.ecomind.community.domain.valueobjects.PostReactionType
import pe.greenminds.ecomind.community.presentation.states.CommunityNewsUiState

@HiltViewModel
class CommunityNewsViewModel @Inject constructor(
    private val getPosts: GetCommunityPostsUseCase,
    private val getReactions: GetPostReactionsUseCase,
    private val setReaction: SetPostReactionUseCase,
    private val removeReaction: RemovePostReactionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CommunityNewsUiState())
    val state = _state.asStateFlow()

    private var feedJob: Job? = null
    private var reactionJob: Job? = null
    private var generation = 0L
    private var dialogGeneration = 0L

    fun bindCommunity(communityId: Long) {
        if (_state.value.communityId == communityId) return

        generation++
        dialogGeneration++
        feedJob?.cancel()
        reactionJob?.cancel()
        _state.value = CommunityNewsUiState(communityId = communityId)
        refresh()
    }

    fun clear() {
        generation++
        dialogGeneration++
        feedJob?.cancel()
        reactionJob?.cancel()
        _state.value = CommunityNewsUiState()
    }

    fun refresh() {
        val communityId = _state.value.communityId ?: return
        if (_state.value.isSavingReaction) return

        closeDialog()
        feedJob?.cancel()
        val currentGeneration = generation

        _state.update {
            it.copy(
                isLoading = true,
                error = null,
                errorRes = null
            )
        }

        feedJob = viewModelScope.launch {
            val result = getPosts(communityId)
            if (currentGeneration != generation) return@launch

            result.fold(
                onSuccess = { posts ->
                    _state.update {
                        it.copy(
                            posts = posts,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            error = error.message,
                            errorRes = if (error.message == null) {
                                R.string.news_load_error
                            } else {
                                null
                            }
                        )
                    }
                }
            )
        }
    }

    fun openReactions(postId: Long, picker: Boolean) {
        if (_state.value.isSavingReaction || _state.value.isLoading) return
        if (_state.value.posts.none { it.id == postId }) return

        reactionJob?.cancel()
        dialogGeneration++

        _state.update {
            it.copy(
                selectedPostId = postId,
                showPicker = picker,
                reactions = null,
                isLoadingReactions = true,
                isSavingReaction = false,
                reactionError = null,
                reactionErrorRes = null
            )
        }

        loadReactions(postId)
    }

    fun retryReactions() {
        val postId = _state.value.selectedPostId ?: return
        if (_state.value.isSavingReaction) return

        reactionJob?.cancel()
        dialogGeneration++
        _state.update {
            it.copy(
                reactions = null,
                isLoadingReactions = true,
                reactionError = null,
                reactionErrorRes = null
            )
        }
        loadReactions(postId)
    }

    private fun loadReactions(postId: Long) {
        val currentGeneration = generation
        val currentDialog = dialogGeneration

        reactionJob = viewModelScope.launch {
            val result = getReactions(postId)
            if (!isCurrent(currentGeneration, currentDialog, postId)) {
                return@launch
            }

            result.fold(
                onSuccess = { reactions ->
                    _state.update { current ->
                        current.copy(
                            reactions = reactions,
                            isLoadingReactions = false,
                            posts = current.posts.map {
                                if (it.id == postId) {
                                    it.copy(likes = reactions.total)
                                } else {
                                    it
                                }
                            }
                        )
                    }
                },
                onFailure = { error ->
                    showReactionError(error)
                }
            )
        }
    }

    fun chooseReaction(type: PostReactionType) {
        val current = _state.value
        val postId = current.selectedPostId ?: return
        val reactions = current.reactions ?: return

        if (current.isSavingReaction || current.isLoadingReactions) return
        if (reactions.ownReaction == type) return

        mutateReaction(postId) {
            setReaction(postId, type)
        }
    }

    fun removeOwnReaction() {
        val current = _state.value
        val postId = current.selectedPostId ?: return

        if (current.reactions?.ownReaction == null) return
        if (current.isSavingReaction || current.isLoadingReactions) return

        mutateReaction(postId) {
            removeReaction(postId)
        }
    }

    private fun mutateReaction(
        postId: Long,
        operation: suspend () -> Result<Unit>
    ) {
        reactionJob?.cancel()
        val currentGeneration = generation
        val currentDialog = dialogGeneration

        _state.update {
            it.copy(
                isSavingReaction = true,
                reactionError = null,
                reactionErrorRes = null
            )
        }

        reactionJob = viewModelScope.launch {
            val mutation = operation()
            if (!isCurrent(currentGeneration, currentDialog, postId)) {
                return@launch
            }

            val failure = mutation.exceptionOrNull()
            if (failure != null) {
                showReactionError(failure)
                return@launch
            }

            val refreshed = getReactions(postId)
            if (!isCurrent(currentGeneration, currentDialog, postId)) {
                return@launch
            }

            refreshed.fold(
                onSuccess = { reactions ->
                    _state.update { current ->
                        current.copy(
                            reactions = reactions,
                            isSavingReaction = false,
                            posts = current.posts.map {
                                if (it.id == postId) {
                                    it.copy(likes = reactions.total)
                                } else {
                                    it
                                }
                            }
                        )
                    }
                },
                onFailure = { error ->
                    showReactionError(error)
                }
            )
        }
    }

    private fun showReactionError(error: Throwable) {
        _state.update {
            it.copy(
                reactions = null,
                isLoadingReactions = false,
                isSavingReaction = false,
                reactionError = error.message,
                reactionErrorRes = if (error.message == null) {
                    R.string.news_reactions_error
                } else {
                    null
                }
            )
        }
    }

    private fun isCurrent(
        currentGeneration: Long,
        currentDialog: Long,
        postId: Long
    ): Boolean =
        currentGeneration == generation &&
                currentDialog == dialogGeneration &&
                _state.value.selectedPostId == postId

    fun closeDialog() {
        if (_state.value.isSavingReaction) return

        dialogGeneration++
        reactionJob?.cancel()
        _state.update {
            it.copy(
                selectedPostId = null,
                reactions = null,
                isLoadingReactions = false,
                isSavingReaction = false,
                reactionError = null,
                reactionErrorRes = null
            )
        }
    }
}