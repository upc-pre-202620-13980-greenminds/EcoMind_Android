package pe.greenminds.ecomind.learning.interfaces

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import pe.greenminds.ecomind.learning.domain.*

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
            onCategory = viewModel::setCategory,
            onType = viewModel::setType,
            onFavorites = viewModel::toggleFavoritesOnly,
            onApply = { showFilters = false }
        )
        return
    }

    Column(Modifier.fillMaxSize().background(Color.White).padding(horizontal = 16.dp)) {
        SearchBar(
            query = state.query,
            onQuery = viewModel::setQuery,
            onFilters = { showFilters = true }
        )
        if (state.favoritesOnly) {
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
private fun MaterialCard(material: EducationalMaterial, onOpen: () -> Unit, onFavorite: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().height(310.dp).clickable(onClick = onOpen),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        AsyncImage(
            model = material.imageUrl, contentDescription = material.title,
            modifier = Modifier.fillMaxWidth().height(122.dp), contentScale = ContentScale.Crop
        )
        Column(Modifier.fillMaxSize().padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(material.title, Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(if (material.downloadable) "⇩" else "", fontSize = 23.sp)
                Surface(Modifier.padding(start = 5.dp).clickable(onClick = onFavorite), color = if (material.favorite) Color(0xFFFFD9DD) else Color(0xFFF0F0F2), shape = CircleShape) {
                    Text(if (material.favorite) "♥" else "♡", Modifier.padding(7.dp), color = if (material.favorite) Color(0xFFE43B3B) else Color.Gray, fontSize = 18.sp)
                }
            }
            Row(Modifier.padding(top = 7.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Tag(material.materialType.label(), TypeBlue, Color(0xFF0784C9))
                Tag(material.category.label(), CategoryGreen, LearningGreen)
            }
            Text(material.description, Modifier.padding(top = 18.dp), fontSize = 12.sp, lineHeight = 17.sp, maxLines = 4, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.weight(1f))
            Text("${material.durationMinutes} min", Modifier.align(Alignment.End), fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable private fun Tag(text: String, background: Color, foreground: Color) {
    Surface(color = background, shape = RoundedCornerShape(18.dp)) {
        Text(text, Modifier.padding(horizontal = 12.dp, vertical = 5.dp), color = foreground, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LearningFilters(
    state: LearningUiState,
    onCategory: (MaterialCategory?) -> Unit,
    onType: (MaterialType?) -> Unit,
    onFavorites: () -> Unit,
    onApply: () -> Unit
) {
    Column(Modifier.fillMaxSize().background(Color.White).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Filtros", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = LearningGreen)
        FilterToggle("Solo favoritos", state.favoritesOnly, onFavorites)
        Text("Categoría", fontWeight = FontWeight.SemiBold)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = state.category == null, onClick = { onCategory(null) }, label = { Text("Todas") })
            MaterialCategory.entries.forEach { category ->
                FilterChip(selected = state.category == category, onClick = { onCategory(category) }, label = { Text(category.label()) })
            }
        }
        Text("Tipo", fontWeight = FontWeight.SemiBold)
        MaterialType.entries.forEach { type -> FilterToggle(type.label(), state.type == type) { onType(if (state.type == type) null else type) } }
        Spacer(Modifier.weight(1f))
        Button(onClick = onApply, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = LearningGreen), shape = RoundedCornerShape(6.dp)) {
            Text("Aplicar")
        }
    }
}

@Composable private fun FilterToggle(text: String, checked: Boolean, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked, onCheckedChange = { onClick() }, colors = CheckboxDefaults.colors(checkedColor = LearningGreen))
        Text(text, fontSize = 13.sp)
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
