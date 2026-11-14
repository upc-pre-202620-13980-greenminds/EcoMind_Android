package pe.greenminds.ecomind.learning.interfaces

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.learning.domain.TutorialProgress
import pe.greenminds.ecomind.learning.infrastructure.TutorialRepository
import pe.greenminds.ecomind.R
import javax.inject.Inject

private val TutorialGreen = Color(0xFF6CAB4F)

data class TutorialUiState(
    val loading: Boolean = false,
    val progress: TutorialProgress? = null,
    val confirmation: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class TutorialViewModel @Inject constructor(private val repository: TutorialRepository) : ViewModel() {
    private val _state = MutableStateFlow(TutorialUiState())
    val state = _state.asStateFlow()
    private var loaded = false

    fun load() {
        if (loaded) return
        loaded = true
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            repository.progress().fold(
                { progress -> _state.value = TutorialUiState(progress = progress) },
                { _state.value = TutorialUiState(error = it.message) }
            )
        }
    }

    fun next() = viewModelScope.launch {
        val progress = _state.value.progress ?: return@launch
        _state.update { it.copy(loading = true, error = null) }
        val request = if (progress.currentStep == progress.totalSteps) repository.complete() else repository.next()
        request.fold(
            { updated -> _state.value = TutorialUiState(progress = updated, confirmation = updated.completed) },
            { error -> _state.update { it.copy(loading = false, error = error.message) } }
        )
    }

    fun closeConfirmation() { _state.update { it.copy(confirmation = false) } }
}

private data class TutorialStep(val image: Int, val title: String, val description: String, val logo: Boolean = false)
private val tutorialSteps = listOf(
    TutorialStep(R.drawable.img_logo_ecomind, "¡Bienvenido a EcoMind!", "Hagamos un recorrido rápido para mostrarte el camino.", true),
    TutorialStep(R.drawable.ic_nav_main_menu_active, "Misiones", "Completa desafíos y gana recompensas mientras ayudas al planeta."),
    TutorialStep(R.drawable.ic_nav_profile_active, "Perfil", "Sigue tu progreso, tus logros y tu impacto ecológico."),
    TutorialStep(R.drawable.ic_nav_community_active, "Comunidad", "Conecta con otras personas y comparte tu viaje sostenible."),
    TutorialStep(R.drawable.ic_nav_store_active, "Tienda", "Personaliza tu experiencia con avatares, cosméticos y potenciadores."),
    TutorialStep(R.drawable.img_tile_videos_default, "Aprendizaje", "Explora materiales educativos sobre sostenibilidad y guarda tus favoritos."),
    TutorialStep(R.drawable.img_logo_ecomind, "¡Estás listo!", "Comienza tu recorrido y marca la diferencia para nuestro planeta.", true)
)

@Composable
fun TutorialOverlay(active: Boolean, viewModel: TutorialViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(active) { if (active) viewModel.load() }
    val progress = state.progress
    if (!active || progress == null || progress.completed && !state.confirmation) return

    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.55f)), contentAlignment = Alignment.Center) {
        Card(
            Modifier.fillMaxWidth(0.88f),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            if (state.confirmation) {
                Column(Modifier.padding(horizontal = 28.dp, vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✓", color = Color.White, fontSize = 34.sp, textAlign = TextAlign.Center,
                        modifier = Modifier.size(64.dp).background(TutorialGreen, CircleShape).wrapContentSize())
                    Text("¡Tutorial completado!", Modifier.padding(top = 20.dp), color = TutorialGreen, fontWeight = FontWeight.Bold, fontSize = 23.sp)
                    Text("Ya conoces las funciones principales de EcoMind.", Modifier.padding(top = 10.dp), color = Color(0xFF555555), textAlign = TextAlign.Center, lineHeight = 22.sp)
                    Button(viewModel::closeConfirmation, Modifier.fillMaxWidth().padding(top = 24.dp), colors = ButtonDefaults.buttonColors(containerColor = TutorialGreen), shape = RoundedCornerShape(12.dp)) {
                        Text("Comenzar", fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                val step = tutorialSteps[(progress.currentStep - 1).coerceIn(tutorialSteps.indices)]
                Column(Modifier.padding(horizontal = 28.dp, vertical = 28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${progress.currentStep} / ${progress.totalSteps}", color = Color(0xFF888888), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Surface(
                        modifier = Modifier.padding(top = 20.dp).then(if (step.logo) Modifier.width(190.dp).height(72.dp) else Modifier.size(86.dp)),
                        color = if (step.logo) Color.Transparent else Color(0xFFF1F7EE),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Image(
                            painter = painterResource(step.image),
                            contentDescription = step.title,
                            modifier = Modifier.padding(if (step.logo) 4.dp else 17.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                    Text(step.title, Modifier.padding(top = 18.dp), color = TutorialGreen, fontWeight = FontWeight.Bold, fontSize = 22.sp, textAlign = TextAlign.Center)
                    Text(step.description, Modifier.padding(top = 10.dp), color = Color(0xFF555555), fontSize = 15.sp, lineHeight = 23.sp, textAlign = TextAlign.Center)
                    state.error?.let { Text(it, Modifier.padding(top = 10.dp), color = MaterialTheme.colorScheme.error, fontSize = 12.sp) }
                    Button(
                        onClick = viewModel::next,
                        enabled = !state.loading,
                        modifier = Modifier.fillMaxWidth().padding(top = 24.dp).height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = TutorialGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (state.loading) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        else Text(if (progress.currentStep == progress.totalSteps) "Finalizar" else "Siguiente", fontWeight = FontWeight.Bold)
                    }
                    Text("Completa los pasos en orden para continuar", Modifier.padding(top = 12.dp), color = Color(0xFF999999), fontSize = 11.sp)
                }
            }
        }
    }
}
