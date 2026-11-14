package pe.greenminds.ecomind.learning.interfaces

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.learning.domain.EducationalMaterial
import pe.greenminds.ecomind.learning.domain.LearningRepository
import pe.greenminds.ecomind.users.interfaces.editprofile.SubScreenHeader

private val FavoritesGreen = Color(0xFF69A44E)

data class FavoriteMaterialsUiState(
    val loading: Boolean = true,
    val materials: List<EducationalMaterial> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class FavoriteMaterialsViewModel @Inject constructor(
    private val repository: LearningRepository
) : ViewModel() {
    private val _state = MutableStateFlow(FavoriteMaterialsUiState())
    val state: StateFlow<FavoriteMaterialsUiState> = _state.asStateFlow()

    init {
        loadFavorites()
    }

    fun loadFavorites() = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null) }
        repository.getFavorites().fold(
            onSuccess = { materials ->
                _state.value = FavoriteMaterialsUiState(
                    loading = false,
                    materials = materials
                )
            },
            onFailure = { error ->
                _state.update {
                    it.copy(
                        loading = false,
                        error = error.message ?: "No se pudieron cargar tus favoritos"
                    )
                }
            }
        )
    }

    fun removeFavorite(material: EducationalMaterial) = viewModelScope.launch {
        repository.toggleFavorite(material.id).onSuccess { favorite ->
            if (!favorite) {
                _state.update { state ->
                    state.copy(materials = state.materials.filterNot { it.id == material.id })
                }
            } else {
                loadFavorites()
            }
        }
    }
}

@Composable
fun FavoriteMaterialsScreen(
    onBack: () -> Unit,
    onOpenMaterial: (Long) -> Unit,
    viewModel: FavoriteMaterialsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp)
    ) {
        SubScreenHeader(title = "Materiales favoritos", onBack = onBack)

        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = FavoritesGreen)
            }

            state.error != null -> Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = state.error.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                TextButton(onClick = viewModel::loadFavorites) {
                    Text("Reintentar", color = FavoritesGreen)
                }
            }

            state.materials.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aún no tienes materiales favoritos.",
                    color = Color(0xFF777777),
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }

            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
            ) {
                items(state.materials, key = { it.id }) { material ->
                    MaterialCard(
                        material = material,
                        onOpen = { onOpenMaterial(material.id) },
                        onFavorite = { viewModel.removeFavorite(material) }
                    )
                }
            }
        }
    }
}
