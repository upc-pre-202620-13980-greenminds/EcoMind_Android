package pe.greenminds.ecomind.gamification.interfaces.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.gamification.application.GetAchievementShareUseCase
import pe.greenminds.ecomind.gamification.application.GetGamificationOverviewUseCase
import pe.greenminds.ecomind.gamification.application.ShareAchievementUseCase
import pe.greenminds.ecomind.gamification.domain.model.AchievementSessionRequiredException
import javax.inject.Inject

@HiltViewModel
class ShareAchievementViewModel @Inject constructor(
    private val getOverview: GetGamificationOverviewUseCase,
    private val getShare: GetAchievementShareUseCase,
    private val shareAchievement: ShareAchievementUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ShareAchievementUiState())
    val state: StateFlow<ShareAchievementUiState> = _state.asStateFlow()
    private var loadJob: Job? = null

    fun loadShare(awardId: String) {
        if (_state.value.isSharing) return
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.value = ShareAchievementUiState()
            try {
                val overview = getOverview().getOrThrow()
                val share = getShare(awardId).getOrThrow()
                _state.value = ShareAchievementUiState(
                    isLoading = false,
                    communities = overview.groups.filter { it.scope == "COMMUNITY" },
                    selectedCommunityId = share?.communityId,
                    share = share,
                    isSimulated = overview.simulated,
                    isSubmitted = share != null
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                showError(exception)
            }
        }
    }

    fun onCommunitySelected(communityId: Long) {
        _state.update { currentState ->
            if (!currentState.isSharing && !currentState.isSubmitted &&
                currentState.communities.any { it.id == communityId }
            ) {
                currentState.copy(selectedCommunityId = communityId)
            } else {
                currentState
            }
        }
    }

    fun share(awardId: String) {
        val current = _state.value
        val communityId = current.selectedCommunityId ?: return
        if (current.isSharing || current.isLoading || current.isSimulated ||
            current.share?.status == "PUBLISHED"
        ) return

        _state.update { it.copy(isSharing = true, hasError = false, isSubmitted = true) }
        loadJob = viewModelScope.launch {
            try {
                val result = shareAchievement(awardId, communityId).getOrThrow()
                _state.update { it.copy(isSharing = false, share = result) }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                showError(exception)
            }
        }
    }

    private fun showError(exception: Throwable) {
        if (exception is CancellationException) throw exception
        _state.update { currentState ->
            currentState.copy(
                isLoading = false,
                isSharing = false,
                hasError = true,
                sessionRequired = exception is AchievementSessionRequiredException
            )
        }
    }
}
