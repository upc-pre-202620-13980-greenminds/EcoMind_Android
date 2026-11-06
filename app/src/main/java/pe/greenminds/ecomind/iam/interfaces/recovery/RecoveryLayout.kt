package pe.greenminds.ecomind.iam.interfaces.recovery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.components.EcoMindLogo

@Composable
internal fun RecoveryLayout(
    title: String,
    description: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.Start).sizeIn(minWidth = 48.dp, minHeight = 48.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_back),
                contentDescription = stringResource(R.string.back)
            )
        }
        EcoMindLogo()
        Spacer(Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth().semantics { heading() }
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite }
        )
        Spacer(Modifier.height(24.dp))
        content()
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
internal fun RecoveryError(message: String) {
    Text(
        text = message,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            .semantics { liveRegion = LiveRegionMode.Polite }
    )
}
