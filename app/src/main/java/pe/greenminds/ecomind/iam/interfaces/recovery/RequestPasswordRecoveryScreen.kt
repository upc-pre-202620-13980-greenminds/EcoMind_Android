package pe.greenminds.ecomind.iam.interfaces.recovery

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.iam.domain.model.EmailAddress
import pe.greenminds.ecomind.shared.interfaces.components.LabeledTextField
import pe.greenminds.ecomind.shared.interfaces.components.PrimaryButton
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme

@Composable
fun RequestPasswordRecoveryScreen(
    onBack: () -> Unit,
    viewModel: RequestPasswordRecoveryViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    RequestPasswordRecoveryContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onRequestRecovery = viewModel::requestRecovery,
        onBack = onBack
    )
}

@Composable
fun RequestPasswordRecoveryContent(
    state: RequestPasswordRecoveryUiState,
    onEmailChange: (String) -> Unit,
    onRequestRecovery: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val submit = {
        focusManager.clearFocus()
        onRequestRecovery()
    }
    RecoveryLayout(
        title = stringResource(
            if (state.isRequested) R.string.recovery_check_email_title else R.string.recovery_request_title
        ),
        description = if (state.isRequested) {
            stringResource(
                R.string.recovery_check_email_description,
                EmailAddress.mask(EmailAddress.normalize(state.email))
            )
        } else stringResource(R.string.recovery_request_description),
        onBack = onBack,
        modifier = modifier
    ) {
        if (!state.isRequested) {
            LabeledTextField(
                label = stringResource(R.string.sign_in_email_label),
                value = state.email,
                onValueChange = onEmailChange,
                errorMessage = state.emailError?.let { stringResource(it) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { submit() })
            )
        }
        state.formError?.let { RecoveryError(stringResource(it)) }
        Spacer(Modifier.height(24.dp))
        if (state.isRequested) {
            PrimaryButton(
                text = stringResource(R.string.recovery_back_to_sign_in),
                onClick = onBack
            )
            TextButton(onClick = submit, enabled = !state.isLoading) {
                Text(
                    text = stringResource(if (state.isLoading) R.string.loading else R.string.recovery_resend_button),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        } else {
            PrimaryButton(
                text = stringResource(R.string.recovery_request_button),
                onClick = submit,
                isLoading = state.isLoading
            )
            TextButton(onClick = onBack) {
                Text(
                    text = stringResource(R.string.recovery_back_to_sign_in),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun RequestPasswordRecoveryPreview() {
    EcoMindTheme {
        RequestPasswordRecoveryContent(RequestPasswordRecoveryUiState(), {}, {}, {})
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun RequestPasswordRecoverySentPreview() {
    EcoMindTheme {
        RequestPasswordRecoveryContent(
            RequestPasswordRecoveryUiState(email = "student@example.com", isRequested = true), {}, {}, {}
        )
    }
}
