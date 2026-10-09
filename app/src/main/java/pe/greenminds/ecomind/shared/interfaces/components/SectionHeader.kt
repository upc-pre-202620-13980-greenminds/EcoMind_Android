package pe.greenminds.ecomind.shared.interfaces.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle

@Composable
fun SectionHeader(title: String, onBack: () -> Unit) {
    Box(Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
        IconButton(onBack, Modifier.align(Alignment.CenterStart)) {
            Icon(painterResource(R.drawable.ic_chevron_back), stringResource(R.string.achievements_back), tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        }
        Text(title, Modifier.align(Alignment.Center).padding(horizontal = 32.dp).semantics { heading() },
            style = interTextStyle(20, FontWeight.Bold), color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center)
    }
}
