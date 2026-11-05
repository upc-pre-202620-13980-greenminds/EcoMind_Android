package pe.greenminds.ecomind.learning.interfaces

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.learning.domain.*
import javax.inject.Inject

data class LearningUiState(
    val loading: Boolean = true,
    val materials: List<EducationalMaterial> = emptyList(),
    val query: String = "",
    val category: MaterialCategory? = null,
    val type: MaterialType? = null,
    val favoritesOnly: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LearningViewModel @Inject constructor(private val repository: LearningRepository) : ViewModel() {
    private val _state = MutableStateFlow(LearningUiState())
    val state: StateFlow<LearningUiState> = _state.asStateFlow()
    private var searchJob: Job? = null
    init { load() }

    fun setQuery(value: String) {
        _state.update { it.copy(query = value) }
        searchJob?.cancel(); searchJob = viewModelScope.launch { delay(250); load() }
    }
    fun setCategory(value: MaterialCategory?) { _state.update { it.copy(category = value) }; load() }
    fun setType(value: MaterialType?) { _state.update { it.copy(type = value) }; load() }
    fun toggleFavoritesOnly() { _state.update { it.copy(favoritesOnly = !it.favoritesOnly) }; load() }
    fun retry() = load()
    fun toggleFavorite(material: EducationalMaterial) = viewModelScope.launch {
        repository.toggleFavorite(material.id).onSuccess { favorite ->
            _state.update { state -> state.copy(materials = state.materials.map { if (it.id == material.id) it.copy(favorite = favorite) else it }.filter { !state.favoritesOnly || it.favorite }) }
        }
    }

    private fun load() {
        val snapshot = _state.value
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            val result = if (snapshot.favoritesOnly) repository.getFavorites()
            else repository.getMaterials(snapshot.query, snapshot.category, snapshot.type)
            result.fold(
                onSuccess = { items -> _state.update { it.copy(loading = false, materials = items) } },
                onFailure = { error -> _state.update { it.copy(loading = false, error = error.message ?: "No se pudieron cargar los materiales") } }
            )
        }
    }
}
