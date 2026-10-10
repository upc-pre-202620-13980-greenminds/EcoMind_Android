package pe.greenminds.ecomind.iam.interfaces.signin

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.components.EcoMindLogo
import pe.greenminds.ecomind.shared.interfaces.components.LabeledTextField
import pe.greenminds.ecomind.shared.interfaces.components.PrimaryButton
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme

@Composable
fun SignInScreen(
    onSignedIn: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    viewModel: SignInViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) {
            onSignedIn()
        }
    }

    SignInContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onSignIn = viewModel::signIn,
        onNavigateToSignUp = onNavigateToSignUp
    )
}

// Receives the state and the events, so it can be shown in the preview without a ViewModel
@Composable
private fun SignInContent(
    state: SignInUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignIn: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // Keeps the content away from the system bars and above the keyboard
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(64.dp))
        EcoMindLogo()
        Spacer(modifier = Modifier.height(56.dp))

        LabeledTextField(
            label = stringResource(R.string.sign_in_email_label),
            value = state.email,
            onValueChange = onEmailChange,
            errorMessage = state.emailError?.let { stringResource(it) },
            // "Next" moves the focus to the password field
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        LabeledTextField(
            label = stringResource(R.string.sign_in_password_label),
            value = state.password,
            onValueChange = onPasswordChange,
            errorMessage = state.passwordError?.let { stringResource(it) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            // "Done" hides the keyboard and sends the form
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onSignIn()
                }
            )
        )

        if (state.formError != null) {
            Text(
                text = stringResource(state.formError),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                // The screen reader announces the message as soon as it appears
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, top = 8.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        PrimaryButton(
            text = stringResource(R.string.sign_in_button),
            onClick = onSignIn,
            isLoading = state.isLoading
        )

        Spacer(modifier = Modifier.height(56.dp))
        Text(
            text = stringResource(R.string.sign_in_no_account),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        TextButton(onClick = onNavigateToSignUp) {
            Text(
                text = stringResource(R.string.sign_in_sign_up_link),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SignInContentPreview() {
    EcoMindTheme {
        SignInContent(
            state = SignInUiState(),
            onEmailChange = {},
            onPasswordChange = {},
            onSignIn = {},
            onNavigateToSignUp = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SignInContentErrorPreview() {
    EcoMindTheme {
        SignInContent(
            state = SignInUiState(
                emailError = R.string.error_email_invalid,
                passwordError = R.string.error_password_required,
                formError = R.string.error_invalid_credentials
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onSignIn = {},
            onNavigateToSignUp = {}
        )
    }
}
