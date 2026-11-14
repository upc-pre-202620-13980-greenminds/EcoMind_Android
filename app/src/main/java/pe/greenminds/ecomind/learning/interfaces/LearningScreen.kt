package pe.greenminds.ecomind.learning.interfaces

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import pe.greenminds.ecomind.learning.domain.*
import java.io.File

private val LearningGreen = Color(0xFF65A94D)
private val TypeBlue = Color(0xFFA7DDFB)
private val CategoryGreen = Color(0xFFCFFFC0)

@Composable
fun LearningScreen(onOpenMaterial: (Long) -> Unit, viewModel: LearningViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showFilters by rememberSaveable { mutableStateOf(false) }
    if (showFilters) {
        LearningFilters(
            state = state,
            onQuery = viewModel::setQuery,
            onCategory = viewModel::setCategory,
            onType = viewModel::setType,
            onFavorites = viewModel::toggleFavoritesOnly,
            onRecent = viewModel::toggleRecentlyViewedOnly,
            onAge = viewModel::setRecommendedAge,
            onDuration = viewModel::setDuration,
            onApply = { viewModel.applyFilters(); showFilters = false }
        )
        return
    }

    Column(Modifier.fillMaxSize().background(Color.White).padding(horizontal = 16.dp)) {
        SearchBar(
            query = state.query,
            onQuery = viewModel::setQuery,
            onFilters = { showFilters = true }
        )
        if (state.recentlyViewedOnly) {
            FilterLabel("Vistos recientemente")
        } else if (state.favoritesOnly) {
            FilterLabel("Favoritos")
        } else if (state.category != null || state.type != null) {
            FilterLabel("Filtros aplicados")
        }
        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = LearningGreen)
            }
            state.error != null -> ErrorContent(state.error.orEmpty(), viewModel::retry)
            state.materials.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No se encontraron materiales", color = Color.Gray)
            }
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
            ) {
                items(state.materials, key = { it.id }) { material ->
                    MaterialCard(material, { onOpenMaterial(material.id) }, { viewModel.toggleFavorite(material) })
                }
            }
        }
    }
}

@Composable
private fun SearchBar(query: String, onQuery: (String) -> Unit, onFilters: () -> Unit) {
    Surface(Modifier.fillMaxWidth().padding(top = 16.dp), shape = RoundedCornerShape(28.dp), color = Color(0xFFF0F0F2)) {
        Row(Modifier.height(54.dp).padding(horizontal = 15.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("☰", modifier = Modifier.clickable(onClick = onFilters).padding(5.dp), fontSize = 23.sp, color = Color(0xFF44444D))
            TextField(
                value = query, onValueChange = onQuery, modifier = Modifier.weight(1f),
                placeholder = { Text("Buscar", color = Color(0xFF54515B)) },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                )
            )
            Text("⌕", fontSize = 29.sp, color = Color(0xFF44444D))
        }
    }
}

@Composable private fun FilterLabel(text: String) {
    Surface(Modifier.padding(top = 12.dp), color = LearningGreen, shape = RoundedCornerShape(20.dp)) {
        Text(text, Modifier.padding(horizontal = 20.dp, vertical = 7.dp), color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
internal fun MaterialCard(material: EducationalMaterial, onOpen: () -> Unit, onFavorite: () -> Unit) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth().height(272.dp).clickable(onClick = onOpen),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        AsyncImage(
            model = material.imageUrl, contentDescription = material.title,
            modifier = Modifier.fillMaxWidth().height(116.dp), contentScale = ContentScale.Crop
        )
        Column(Modifier.fillMaxSize().padding(9.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(material.title, Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (material.downloadable) {
                    Image(DownloadIcon, "Descargar", Modifier.size(28.dp).clickable { downloadMaterial(context, material) }.padding(4.dp))
                }
                Surface(Modifier.padding(start = 5.dp).clickable(onClick = onFavorite), color = if (material.favorite) Color(0xFFFFD9DD) else Color(0xFFF0F0F2), shape = CircleShape) {
                    Icon(FavoriteIcon, "Favorito", Modifier.padding(6.dp).size(18.dp), tint = if (material.favorite) Color(0xFFE43B3B) else Color.Gray)
                }
            }
            Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Tag(material.materialType.label(), TypeBlue, Color(0xFF0784C9))
                Tag(material.category.label(), CategoryGreen, LearningGreen)
            }
            Text(material.description, Modifier.padding(top = 12.dp), fontSize = 10.sp, lineHeight = 14.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.weight(1f))
            Text("${material.durationMinutes} min", Modifier.align(Alignment.End), fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable private fun Tag(text: String, background: Color, foreground: Color) {
    Surface(color = background, shape = RoundedCornerShape(18.dp)) {
        Text(text, Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = foreground, fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun LearningFilters(
    state: LearningUiState,
    onQuery: (String) -> Unit,
    onCategory: (MaterialCategory?) -> Unit,
    onType: (MaterialType?) -> Unit,
    onFavorites: () -> Unit,
    onRecent: () -> Unit,
    onAge: (RecommendedAge?) -> Unit,
    onDuration: (Int?, Int?) -> Unit,
    onApply: () -> Unit
) {
    var categoriesExpanded by rememberSaveable { mutableStateOf(false) }
    var typesExpanded by rememberSaveable { mutableStateOf(true) }
    var ageExpanded by rememberSaveable { mutableStateOf(false) }
    var timeExpanded by rememberSaveable { mutableStateOf(false) }

    Column(
        Modifier.fillMaxSize().background(Color.White).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
    ) {
        SearchBar(query = state.query, onQuery = onQuery, onFilters = onApply)
        Spacer(Modifier.height(14.dp))
        CompactCheckRow("Favoritos", state.favoritesOnly, onFavorites)
        CompactCheckRow("Vistos recientemente", state.recentlyViewedOnly, onRecent)
        Spacer(Modifier.height(12.dp))

        FilterSectionCard(
            title = "Categorías temáticas",
            expanded = categoriesExpanded,
            onToggle = { categoriesExpanded = !categoriesExpanded }
        ) {
            CompactCheckRow("Todas", state.category == null) { onCategory(null) }
            MaterialCategory.entries.forEach { category ->
                CompactCheckRow(category.label(), state.category == category) {
                    onCategory(if (state.category == category) null else category)
                }
            }
        }
        Spacer(Modifier.height(14.dp))

        FilterSectionCard(
            title = "Tipo",
            expanded = typesExpanded,
            onToggle = { typesExpanded = !typesExpanded }
        ) {
            listOf(
                MaterialType.VIDEO to "Video",
                MaterialType.TEXT to "Texto",
                MaterialType.INFOGRAPHIC to "Infografía"
            ).forEach { (type, label) ->
                CompactCheckRow(label, state.type == type) {
                    onType(if (state.type == type) null else type)
                }
            }
        }
        Spacer(Modifier.height(14.dp))

        FilterSectionCard(
            title = "Edad recomendada",
            expanded = ageExpanded,
            onToggle = { ageExpanded = !ageExpanded }
        ) {
            RecommendedAge.entries.forEach { age ->
                val label = when (age) { RecommendedAge.CHILDREN -> "Niños"; RecommendedAge.TEENS -> "Adolescentes"; RecommendedAge.ALL -> "Todas las edades" }
                CompactCheckRow(label, state.recommendedAge == age) { onAge(if (state.recommendedAge == age) null else age) }
            }
        }
        Spacer(Modifier.height(14.dp))

        FilterSectionCard(
            title = "Tiempo estimado",
            expanded = timeExpanded,
            onToggle = { timeExpanded = !timeExpanded }
        ) {
            CompactCheckRow("Menos de 5 minutos", state.maxDurationMinutes == 4) { onDuration(null, if (state.maxDurationMinutes == 4) null else 4) }
            CompactCheckRow("5 a 10 minutos", state.minDurationMinutes == 5 && state.maxDurationMinutes == 10) { onDuration(if (state.minDurationMinutes == 5) null else 5, if (state.maxDurationMinutes == 10) null else 10) }
            CompactCheckRow("Más de 10 minutos", state.minDurationMinutes == 11) { onDuration(if (state.minDurationMinutes == 11) null else 11, null) }
        }

        Spacer(Modifier.height(34.dp))
        Button(
            onClick = onApply,
            modifier = Modifier.fillMaxWidth().padding(bottom = 18.dp).height(54.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LearningGreen),
            shape = RoundedCornerShape(7.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text("Aplicar", fontSize = 17.sp)
        }
    }
}

internal fun downloadMaterial(context: Context, material: EducationalMaterial) {
    val safeName = material.title.replace(Regex("[^A-Za-z0-9áéíóúÁÉÍÓÚñÑ _-]"), "").trim().ifBlank { "material" }
    val fileName = "$safeName.pdf"
    val text = buildString {
        appendLine(material.title)
        appendLine()
        appendLine(material.description)
        appendLine()
        appendLine(material.content)
        material.videoUrl?.let { appendLine("\nVideo: $it") }
        material.sourceName?.let { appendLine("\nFuente: $it") }
        material.author?.let { appendLine("Autor: $it") }
        material.sourceUrl?.let { appendLine("Enlace: $it") }
        material.license?.let { appendLine("Licencia/uso: $it") }
    }
    runCatching {
        val pdf = createMaterialPdf(text)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                put(MediaStore.Downloads.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/EcoMind")
            }
            val uri = requireNotNull(context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values))
            context.contentResolver.openOutputStream(uri)?.use { pdf.writeTo(it) }
        } else {
            val folder = requireNotNull(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)).resolve("EcoMind")
            folder.mkdirs()
            File(folder, fileName).outputStream().use { pdf.writeTo(it) }
        }
        pdf.close()
    }.onSuccess {
        Toast.makeText(context, "Material guardado en Descargas/EcoMind", Toast.LENGTH_LONG).show()
    }.onFailure {
        Toast.makeText(context, "No se pudo descargar el material", Toast.LENGTH_LONG).show()
    }
}

private fun createMaterialPdf(text: String): PdfDocument {
    val document = PdfDocument()
    val paint = Paint().apply { color = android.graphics.Color.BLACK; textSize = 12f; isAntiAlias = true }
    val titlePaint = Paint(paint).apply { textSize = 20f; isFakeBoldText = true }
    val width = 595; val height = 842; val margin = 48f; val lineHeight = 18f
    val paragraphs = text.lines(); var pageNumber = 0; var page: PdfDocument.Page? = null; var y = margin
    fun newPage() { page?.let(document::finishPage); pageNumber++; page = document.startPage(PdfDocument.PageInfo.Builder(width, height, pageNumber).create()); y = margin }
    newPage()
    paragraphs.forEachIndexed { index, paragraph ->
        val activePaint = if (index == 0) titlePaint else paint
        val words = paragraph.split(" "); var line = ""
        val wrapped = mutableListOf<String>()
        words.forEach { word -> val candidate = if (line.isEmpty()) word else "$line $word"; if (activePaint.measureText(candidate) > width - margin * 2) { wrapped += line; line = word } else line = candidate }
        wrapped += line
        wrapped.forEach { value -> if (y > height - margin) newPage(); page!!.canvas.drawText(value, margin, y, activePaint); y += if (index == 0) 30f else lineHeight }
        y += 5f
    }
    page?.let(document::finishPage)
    return document
}

@Composable
private fun FilterSectionCard(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(7.dp),
        color = Color.White,
        shadowElevation = 7.dp
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle).padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, Modifier.weight(1f), fontSize = 16.sp, color = Color(0xFF242129))
                Text(if (expanded) "⌃" else "⌄", color = Color(0xFF999999), fontWeight = FontWeight.Bold, fontSize = 25.sp)
            }
            if (expanded) {
                Column(Modifier.padding(start = 17.dp, end = 16.dp, bottom = 12.dp), content = content)
            }
        }
    }
}

@Composable
private fun CompactCheckRow(text: String, checked: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(42.dp).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { onClick() },
            modifier = Modifier.size(38.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = LearningGreen,
                uncheckedColor = Color(0xFF202020),
                checkmarkColor = Color.White
            )
        )
        Text(text, modifier = Modifier.padding(start = 8.dp), fontSize = 14.sp, color = Color(0xFF222222))
    }
}

@Composable private fun ErrorContent(message: String, retry: () -> Unit) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text(message, color = MaterialTheme.colorScheme.error)
        TextButton(onClick = retry) { Text("Reintentar", color = LearningGreen) }
    }
}

internal fun MaterialType.label() = when (this) { MaterialType.TEXT -> "Texto"; MaterialType.VIDEO -> "Video"; MaterialType.INFOGRAPHIC -> "Infografía" }
internal fun MaterialCategory.label() = when (this) { MaterialCategory.RECYCLE -> "Reciclaje"; MaterialCategory.ENERGY -> "Energía"; MaterialCategory.WATER -> "Agua"; MaterialCategory.ENVIRONMENT -> "Ambiente" }
