package pe.greenminds.ecomind.iam.interfaces.terms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.components.PrimaryButton
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme

@Composable
fun TermsScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding().verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        IconButton(onClick = onBack) {
            Icon(painterResource(R.drawable.ic_chevron_back), stringResource(R.string.back))
        }
        Text(
            stringResource(R.string.terms_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(Modifier.height(24.dp))
        SelectionContainer {
            Text(
                stringResource(R.string.terms_body),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Spacer(Modifier.height(24.dp))
        PrimaryButton(text = stringResource(R.string.back), onClick = onBack)
        Spacer(Modifier.height(24.dp))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun TermsPreview() {
    EcoMindTheme { TermsScreen(onBack = {}) }
}
