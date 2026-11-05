package pe.greenminds.ecomind.learning.interfaces

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.learning.domain.*
import javax.inject.Inject

data class LearningDetailState(val loading: Boolean = true, val material: EducationalMaterial? = null, val error: String? = null)

@HiltViewModel
class LearningDetailViewModel @Inject constructor(private val repository: LearningRepository) : ViewModel() {
    private val _state = MutableStateFlow(LearningDetailState())
    val state = _state.asStateFlow()
    fun load(id: Long) = viewModelScope.launch { repository.getMaterial(id).fold(
        { _state.value = LearningDetailState(false, it) },
        { _state.value = LearningDetailState(false, error = it.message) }
    ) }
    fun favorite() = viewModelScope.launch {
        val current = _state.value.material ?: return@launch
        repository.toggleFavorite(current.id).onSuccess { _state.update { state -> state.copy(material = current.copy(favorite = it)) } }
    }
}

@Composable
fun LearningDetailScreen(id: Long, onBack: () -> Unit, viewModel: LearningDetailViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(id) { viewModel.load(id) }
    when {
        state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Color(0xFF65A94D)) }
        state.material == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(state.error ?: "Material no encontrado") }
        else -> MaterialDetail(state.material!!, onBack, viewModel::favorite)
    }
}

@Composable
private fun MaterialDetail(material: EducationalMaterial, onBack: () -> Unit, onFavorite: () -> Unit) {
    val context = LocalContext.current
    Column(Modifier.fillMaxSize().background(Color.White).verticalScroll(rememberScrollState()).padding(18.dp)) {
        Text("‹", modifier = Modifier.clickable(onClick = onBack).padding(vertical = 8.dp), fontSize = 44.sp, fontWeight = FontWeight.Light)
        AsyncImage(model = material.imageUrl, contentDescription = material.title, modifier = Modifier.fillMaxWidth().height(245.dp), contentScale = ContentScale.Crop)
        Row(Modifier.padding(top = 20.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(material.title, Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            if (material.downloadable) {
                Text("⇩", Modifier.clickable { downloadMaterial(context, material) }.padding(5.dp), fontSize = 25.sp)
            }
            Surface(Modifier.padding(start = 10.dp).clickable(onClick = onFavorite), color = if (material.favorite) Color(0xFFFFD9DD) else Color(0xFFF0F0F2), shape = CircleShape) {
                Text(if (material.favorite) "♥" else "♡", Modifier.padding(9.dp), color = Color(0xFFE43B3B), fontSize = 20.sp)
            }
        }
        Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(color = Color(0xFFA7DDFB), shape = RoundedCornerShape(18.dp)) { Text(material.materialType.label(), Modifier.padding(horizontal = 18.dp, vertical = 6.dp), color = Color(0xFF0784C9), fontWeight = FontWeight.Bold) }
            Surface(color = Color(0xFFCFFFC0), shape = RoundedCornerShape(18.dp)) { Text(material.category.label(), Modifier.padding(horizontal = 18.dp, vertical = 6.dp), color = Color(0xFF65A94D), fontWeight = FontWeight.Bold) }
        }
        Row(Modifier.fillMaxWidth().padding(top = 22.dp)) {
            Text(material.description, Modifier.weight(1f), fontSize = 16.sp, lineHeight = 23.sp)
            Text("${material.durationMinutes} min", fontWeight = FontWeight.Bold)
        }
        material.videoUrl?.takeIf { it.isNotBlank() }?.let { videoUrl ->
            Button(
                onClick = {
                    val playableUrl = videoUrl
                        .replace("youtube.com/embed/", "youtube.com/watch?v=")
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(playableUrl)))
                },
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF65A94D)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("▶  Reproducir video", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Text(material.content, Modifier.padding(top = 28.dp, bottom = 28.dp), fontSize = 14.sp, lineHeight = 22.sp)
    }
}
