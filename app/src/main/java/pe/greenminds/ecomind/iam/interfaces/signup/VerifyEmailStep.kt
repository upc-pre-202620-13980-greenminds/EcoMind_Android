package pe.greenminds.ecomind.iam.interfaces.signup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.iam.domain.model.EmailAddress
import pe.greenminds.ecomind.shared.interfaces.components.EcoMindLogo
import pe.greenminds.ecomind.shared.interfaces.components.LabeledTextField
import pe.greenminds.ecomind.shared.interfaces.components.PrimaryButton
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme

@Composable
fun VerifyEmailStep(
    state: SignUpUiState,
    onCodeChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onResendCode: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val maskedEmail = EmailAddress.mask(EmailAddress.normalize(state.email))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_back),
                contentDescription = stringResource(R.string.back)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        EcoMindLogo()
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.verify_email_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(64.dp))
        Text(
            text = stringResource(R.string.verify_email_message, maskedEmail),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(40.dp))
        LabeledTextField(
            label = stringResource(R.string.verify_email_code_label),
            value = state.code,
            onValueChange = onCodeChange,
            errorMessage = state.codeError?.let { stringResource(it) },
            labelStyle = MaterialTheme.typography.titleMedium,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onConfirm()
                }
            )
        )

        if (state.formError != null) {
            Text(
                text = stringResource(state.formError),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, top = 8.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite }
            )
        }

        Spacer(modifier = Modifier.height(56.dp))
        PrimaryButton(
            text = stringResource(R.string.verify_email_button),
            onClick = onConfirm,
            isLoading = state.isLoading
        )
        TextButton(onClick = onResendCode, enabled = !state.isLoading) {
            Text(
                text = stringResource(R.string.verify_email_resend),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        if (state.codeResent) {
            Text(
                text = stringResource(R.string.verify_email_code_resent),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
        TextButton(onClick = onBack) {
            Text(
                text = stringResource(R.string.verify_email_go_back),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun VerifyEmailStepPreview() {
    EcoMindTheme {
        VerifyEmailStep(
            state = SignUpUiState(step = SignUpStep.CODE),
            onCodeChange = {},
            onConfirm = {},
            onResendCode = {},
            onBack = {}
        )
    }
}
