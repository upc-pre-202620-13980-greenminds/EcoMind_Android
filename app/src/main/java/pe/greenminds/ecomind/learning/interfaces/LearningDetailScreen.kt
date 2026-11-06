package pe.greenminds.ecomind.learning.interfaces

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
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
                Image(DownloadIcon, "Descargar", Modifier.size(34.dp).clickable { downloadMaterial(context, material) }.padding(5.dp))
            }
            Surface(Modifier.padding(start = 10.dp).clickable(onClick = onFavorite), color = if (material.favorite) Color(0xFFFFD9DD) else Color(0xFFF0F0F2), shape = CircleShape) {
                Icon(FavoriteIcon, "Favorito", Modifier.padding(9.dp).size(22.dp), tint = if (material.favorite) Color(0xFFE43B3B) else Color.Gray)
            }
        }
        Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(color = Color(0xFFA7DDFB), shape = RoundedCornerShape(18.dp)) { Text(material.materialType.label(), Modifier.padding(horizontal = 18.dp, vertical = 6.dp), color = Color(0xFF0784C9), fontWeight = FontWeight.Bold) }
            Surface(color = Color(0xFFCFFFC0), shape = RoundedCornerShape(18.dp)) { Text(material.category.label(), Modifier.padding(horizontal = 18.dp, vertical = 6.dp), color = Color(0xFF65A94D), fontWeight = FontWeight.Bold) }
        }
        Text(material.description, Modifier.fillMaxWidth().padding(top = 22.dp), fontSize = 16.sp, lineHeight = 23.sp)
        Text("${material.durationMinutes} min", Modifier.align(Alignment.End).padding(top = 8.dp), fontWeight = FontWeight.Bold)
        material.videoUrl?.takeIf { it.isNotBlank() }?.let { videoUrl ->
            Text("Video", Modifier.padding(top = 24.dp, bottom = 10.dp), fontWeight = FontWeight.Bold, fontSize = 17.sp)
            EmbeddedYouTubePlayer(videoUrl)
        }
        Text(material.content, Modifier.padding(top = 28.dp, bottom = 28.dp), fontSize = 14.sp, lineHeight = 22.sp)
        if (!material.sourceName.isNullOrBlank()) {
            HorizontalDivider(color = Color(0xFFE2E2E2))
            Text("Fuente y atribución", Modifier.padding(top = 18.dp), fontWeight = FontWeight.Bold, color = Color(0xFF65A94D))
            Text(material.sourceName, Modifier.padding(top = 8.dp), fontWeight = FontWeight.SemiBold)
            material.author?.takeIf { it.isNotBlank() }?.let { Text("Autor: $it", fontSize = 13.sp) }
            material.license?.takeIf { it.isNotBlank() }?.let { Text(it, fontSize = 12.sp, color = Color.Gray) }
            material.sourceUrl?.takeIf { it.isNotBlank() }?.let { url ->
                Text("Abrir fuente original", Modifier.padding(top = 8.dp, bottom = 24.dp).clickable {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }, color = Color(0xFF0784C9), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun EmbeddedYouTubePlayer(videoUrl: String) {
    val videoId = remember(videoUrl) {
        when {
            "/embed/" in videoUrl -> videoUrl.substringAfter("/embed/").substringBefore('?')
            "v=" in videoUrl -> videoUrl.substringAfter("v=").substringBefore('&')
            "youtu.be/" in videoUrl -> videoUrl.substringAfter("youtu.be/").substringBefore('?')
            else -> videoUrl
        }
    }
    val embedUrl = remember(videoId) {
        "https://www.youtube-nocookie.com/embed/$videoId?playsinline=1&rel=0&controls=1"
    }
    AndroidView(
        modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(12.dp)),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = true
                settings.loadsImagesAutomatically = true
                settings.useWideViewPort = true
                settings.loadWithOverviewMode = true
                CookieManager.getInstance().setAcceptCookie(true)
                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                webChromeClient = WebChromeClient()
                webViewClient = WebViewClient()
                setBackgroundColor(android.graphics.Color.BLACK)
                loadUrl(embedUrl)
            }
        },
        update = { webView ->
            if (webView.url != embedUrl) webView.loadUrl(embedUrl)
        }
    )
}
