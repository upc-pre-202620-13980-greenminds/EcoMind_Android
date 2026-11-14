package pe.greenminds.ecomind.iam.interfaces.recovery

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.components.LabeledTextField
import pe.greenminds.ecomind.shared.interfaces.components.PrimaryButton
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme

@Composable
fun ResetPasswordScreen(
    token: String,
    onBack: () -> Unit,
    onCompleted: () -> Unit,
    viewModel: ResetPasswordViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value
    LaunchedEffect(token) { viewModel.setToken(token) }
    LaunchedEffect(state.isFinished) {
        if (state.isFinished) onCompleted()
    }
    ResetPasswordContent(
        state = state,
        onPasswordChange = viewModel::onPasswordChange,
        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
        onResetPassword = viewModel::resetPassword,
        onBack = onBack,
        onCompleted = viewModel::finish
    )
}

@Composable
fun ResetPasswordContent(
    state: ResetPasswordUiState,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onResetPassword: () -> Unit,
    onBack: () -> Unit,
    onCompleted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val submit = {
        focusManager.clearFocus()
        onResetPassword()
    }
    val title = when {
        state.isCompleted -> R.string.recovery_completed_title
        state.hasInvalidLink -> R.string.recovery_invalid_link_title
        else -> R.string.recovery_reset_title
    }
    val description = when {
        state.isCompleted -> R.string.recovery_completed_description
        state.hasInvalidLink -> R.string.recovery_invalid_link_description
        else -> R.string.recovery_reset_description
    }
    RecoveryLayout(
        title = stringResource(title),
        description = stringResource(description),
        onBack = if (state.isCompleted) onCompleted else onBack,
        modifier = modifier
    ) {
        when {
            state.isCompleted -> {
                state.formError?.let { RecoveryError(stringResource(it)) }
                PrimaryButton(
                    text = stringResource(R.string.sign_in_button),
                    onClick = onCompleted,
                    isLoading = state.isLoading
                )
            }
            state.hasInvalidLink -> PrimaryButton(
                text = stringResource(R.string.recovery_request_new_button),
                onClick = onBack
            )
            else -> {
                LabeledTextField(
                    label = stringResource(R.string.recovery_new_password_label),
                    value = state.password,
                    onValueChange = onPasswordChange,
                    errorMessage = state.passwordError?.let { stringResource(it) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next)
                )
                Spacer(Modifier.height(12.dp))
                LabeledTextField(
                    label = stringResource(R.string.sign_up_confirm_password_label),
                    value = state.confirmPassword,
                    onValueChange = onConfirmPasswordChange,
                    errorMessage = state.confirmPasswordError?.let { stringResource(it) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() })
                )
                state.formError?.let { RecoveryError(stringResource(it)) }
                Spacer(Modifier.height(24.dp))
                PrimaryButton(
                    text = stringResource(R.string.recovery_reset_button),
                    onClick = submit,
                    isLoading = state.isLoading
                )
                TextButton(onClick = onBack) {
                    Text(
                        text = stringResource(R.string.recovery_request_new_button),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ResetPasswordPreview() {
    EcoMindTheme { ResetPasswordContent(ResetPasswordUiState(), {}, {}, {}, {}, {}) }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ResetPasswordInvalidPreview() {
    EcoMindTheme { ResetPasswordContent(ResetPasswordUiState(hasInvalidLink = true), {}, {}, {}, {}, {}) }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ResetPasswordCompletedPreview() {
    EcoMindTheme { ResetPasswordContent(ResetPasswordUiState(isCompleted = true), {}, {}, {}, {}, {}) }
}
