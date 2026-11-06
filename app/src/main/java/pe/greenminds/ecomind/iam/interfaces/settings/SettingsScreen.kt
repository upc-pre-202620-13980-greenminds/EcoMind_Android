package pe.greenminds.ecomind.iam.interfaces.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreen
import pe.greenminds.ecomind.shared.interfaces.theme.SurfaceSoft

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenHelp: () -> Unit,
    viewModel: SettingsViewModel
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    SettingsContent(state, onBack, onOpenTerms, onOpenHelp, viewModel::logout)
}

@Composable
internal fun SettingsContent(
    state: SettingsUiState,
    onBack: () -> Unit,
    onOpenTerms: () -> Unit,
    onOpenHelp: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)
    ) {
        Box(Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(painterResource(R.drawable.ic_chevron_back), stringResource(R.string.back))
            }
            Text(
                stringResource(R.string.top_bar_settings),
                style = MaterialTheme.typography.titleMedium,
                color = EcoGreen,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.settings_account),
            style = MaterialTheme.typography.titleMedium,
            color = EcoGreen
        )
        TextButton(
            onClick = onOpenTerms,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.textButtonColors(
                containerColor = SurfaceSoft,
                contentColor = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.terms_title),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(painterResource(R.drawable.ic_chevron_back), null, Modifier.rotate(180f))
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("Ayuda", style = MaterialTheme.typography.titleMedium, color = EcoGreen)
        TextButton(
            onClick = onOpenHelp,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.textButtonColors(containerColor = SurfaceSoft, contentColor = MaterialTheme.colorScheme.onBackground),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Ayuda y soporte", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                Icon(painterResource(R.drawable.ic_chevron_back), null, Modifier.rotate(180f))
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onLogout,
            enabled = !state.isLoading,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                stringResource(if (state.isLoading) R.string.logging_out else R.string.log_out),
                modifier = Modifier.fillMaxWidth()
            )
        }
        state.errorMessage?.let {
            Text(
                stringResource(it),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite }
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SettingsPreview() {
    EcoMindTheme { SettingsContent(SettingsUiState(), {}, {}, {}, {}) }
}
