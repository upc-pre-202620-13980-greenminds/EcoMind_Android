package pe.greenminds.ecomind.gamification.interfaces.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.gamification.application.*
import pe.greenminds.ecomind.gamification.domain.model.*
import javax.inject.Inject

data class ShareUiState(val loading: Boolean = true, val busy: Boolean = false,
    val communities: List<AchievementGroup> = emptyList(), val selected: Long? = null,
    val share: AchievementShare? = null, val failed: Boolean = false, val sessionRequired: Boolean = false,
    val simulated: Boolean = false, val submitted: Boolean = false)

@HiltViewModel
class ShareAchievementViewModel @Inject constructor(private val overview: GetGamificationOverviewUseCase,
    private val sharing: ShareAchievementUseCase) : ViewModel() {
    private val _state = MutableStateFlow(ShareUiState())
    val state = _state.asStateFlow()
    private var job: Job? = null
    fun load(award: String) {
        if (_state.value.busy) return
        job?.cancel()
        job = viewModelScope.launch {
            _state.value = ShareUiState()
            try {
                val data = overview().getOrThrow()
                val share = sharing.status(award).getOrThrow()
                _state.value = ShareUiState(loading = false, communities = data.groups.filter { it.scope == "COMMUNITY" },
                    selected = share?.communityId, share = share, simulated = data.simulated, submitted = share != null)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { fail(e) }
        }
    }
    fun select(id: Long) {
        val current = _state.value
        if (!current.busy && !current.submitted && current.communities.any { it.id == id }) {
            _state.value = current.copy(selected = id)
        }
    }
    fun send(award: String) {
        val current = _state.value
        val id = current.selected ?: return
        if (current.busy || current.loading || current.simulated || current.share?.status == "PUBLISHED") return
        _state.value = current.copy(busy = true, failed = false, submitted = true)
        job = viewModelScope.launch {
            try {
                val result = sharing(award, id).getOrThrow()
                _state.value = _state.value.copy(busy = false, share = result)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { fail(e) }
        }
    }
    private fun fail(e: Exception) {
        _state.value = _state.value.copy(loading = false, busy = false, failed = true,
            sessionRequired = e is AchievementSessionRequiredException)
    }
}
