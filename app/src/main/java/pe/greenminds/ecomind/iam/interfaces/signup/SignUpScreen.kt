package pe.greenminds.ecomind.iam.interfaces.signup

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.iam.domain.model.SocialRole
import pe.greenminds.ecomind.shared.interfaces.components.EcoMindLogo
import pe.greenminds.ecomind.shared.interfaces.components.LabeledTextField
import pe.greenminds.ecomind.shared.interfaces.components.OptionSelector
import pe.greenminds.ecomind.shared.interfaces.components.PrimaryButton
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme

@Composable
fun SignUpScreen(
    onNavigateToSignIn: () -> Unit,
    onOpenTerms: () -> Unit,
    viewModel: SignUpViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    // The account has no session yet: the user signs in after verifying the email
    LaunchedEffect(state.isVerified) {
        if (state.isVerified) {
            onNavigateToSignIn()
        }
    }

    // In the code step, the system "back" returns to the form instead of leaving the screen
    BackHandler(enabled = state.step == SignUpStep.CODE) {
        viewModel.backToForm()
    }

    when (state.step) {
        SignUpStep.FORM -> SignUpFormStep(
            state = state,
            onNameChange = viewModel::onNameChange,
            onEmailChange = viewModel::onEmailChange,
            onRoleSelected = viewModel::onRoleSelected,
            onPasswordChange = viewModel::onPasswordChange,
            onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
            onTermsAcceptedChange = viewModel::onTermsAcceptedChange,
            onOpenTerms = onOpenTerms,
            onSubmit = viewModel::submitRegistration,
            onNavigateToSignIn = onNavigateToSignIn
        )

        SignUpStep.CODE -> VerifyEmailStep(
            state = state,
            onCodeChange = viewModel::onCodeChange,
            onConfirm = viewModel::verifyCode,
            onResendCode = viewModel::resendCode,
            onBack = viewModel::backToForm
        )
    }
}

@Composable
private fun SignUpFormStep(
    state: SignUpUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onRoleSelected: (SocialRole) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTermsAcceptedChange: (Boolean) -> Unit,
    onOpenTerms: () -> Unit,
    onSubmit: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    // The order of this list is the order of the options on screen
    val roles = listOf(SocialRole.STUDENT, SocialRole.PARENT)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(64.dp))
        EcoMindLogo()
        Spacer(modifier = Modifier.height(56.dp))

        LabeledTextField(
            label = stringResource(R.string.sign_up_name_label),
            value = state.name,
            onValueChange = onNameChange,
            errorMessage = state.nameError?.let { stringResource(it) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        LabeledTextField(
            label = stringResource(R.string.sign_in_email_label),
            value = state.email,
            onValueChange = onEmailChange,
            errorMessage = state.emailError?.let { stringResource(it) },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        OptionSelector(
            label = stringResource(R.string.sign_up_role_label),
            options = listOf(
                stringResource(R.string.sign_up_role_student),
                stringResource(R.string.sign_up_role_parent)
            ),
            selectedIndex = state.role?.let { roles.indexOf(it) },
            onOptionSelected = { index -> onRoleSelected(roles[index]) },
            errorMessage = state.roleError?.let { stringResource(it) }
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
                imeAction = ImeAction.Next
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        LabeledTextField(
            label = stringResource(R.string.sign_up_confirm_password_label),
            value = state.confirmPassword,
            onValueChange = onConfirmPasswordChange,
            errorMessage = state.confirmPasswordError?.let { stringResource(it) },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    onSubmit()
                }
            )
        )

        Spacer(modifier = Modifier.height(8.dp))
        TermsRow(
            accepted = state.termsAccepted,
            onAcceptedChange = onTermsAcceptedChange,
            onOpenTerms = onOpenTerms,
            errorMessage = state.termsError?.let { stringResource(it) }
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

        Spacer(modifier = Modifier.height(16.dp))
        PrimaryButton(
            text = stringResource(R.string.sign_up_button),
            onClick = onSubmit,
            isLoading = state.isLoading
        )

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = stringResource(R.string.sign_up_have_account),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        TextButton(onClick = onNavigateToSignIn) {
            Text(
                text = stringResource(R.string.sign_in_button),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun TermsRow(
    accepted: Boolean,
    onAcceptedChange: (Boolean) -> Unit,
    onOpenTerms: () -> Unit,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    val checkboxDescription = stringResource(R.string.sign_up_terms_checkbox_description)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = accepted,
                onCheckedChange = onAcceptedChange,
                modifier = Modifier.semantics { contentDescription = checkboxDescription }
            )
            Text(
                text = stringResource(R.string.sign_up_terms_prefix),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            TextButton(onClick = onOpenTerms) {
                Text(
                    text = stringResource(R.string.sign_up_terms_link),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    textDecoration = TextDecoration.Underline
                )
            }
        }
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite }
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SignUpFormStepPreview() {
    EcoMindTheme {
        SignUpFormStep(
            state = SignUpUiState(role = SocialRole.STUDENT),
            onNameChange = {},
            onEmailChange = {},
            onRoleSelected = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onTermsAcceptedChange = {},
            onOpenTerms = {},
            onSubmit = {},
            onNavigateToSignIn = {}
        )
    }
}
