package pe.greenminds.ecomind.shared.interfaces.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    Button(
        onClick = onClick,
        // While loading the button cannot be pressed again
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(24.dp),
        // 48dp is the minimum touch target
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
    ) {
        if (isLoading) {
            val loadingDescription = stringResource(R.string.loading)
            CircularProgressIndicator(
                color = LocalContentColor.current,
                strokeWidth = 2.dp,
                modifier = Modifier
                    .size(24.dp)
                    .semantics { contentDescription = loadingDescription }
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PrimaryButtonPreview() {
    EcoMindTheme {
        PrimaryButton(
            text = stringResource(R.string.app_name),
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PrimaryButtonLoadingPreview() {
    EcoMindTheme {
        PrimaryButton(
            text = stringResource(R.string.app_name),
            onClick = {},
            modifier = Modifier.padding(16.dp),
            isLoading = true
        )
    }
}
