package pe.greenminds.ecomind.shared.interfaces.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme

// Notice shown over the current screen for everything that is drawn but not built yet.
// It does not navigate: closing it leaves the screen as it was.
@Composable
fun ComingSoonDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.coming_soon_title),
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Text(
                text = stringResource(R.string.coming_soon_message),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.coming_soon_dismiss))
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Preview
@Composable
private fun ComingSoonDialogPreview() {
    EcoMindTheme {
        ComingSoonDialog(onDismiss = {})
    }
}
