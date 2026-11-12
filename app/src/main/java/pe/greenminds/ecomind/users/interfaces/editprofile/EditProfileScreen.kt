package pe.greenminds.ecomind.users.interfaces.editprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.components.InitialsAvatar
import pe.greenminds.ecomind.shared.interfaces.components.LabeledTextField
import pe.greenminds.ecomind.shared.interfaces.components.PillButton
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreen
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreenDark
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle
import pe.greenminds.ecomind.users.domain.model.SocialRole
import pe.greenminds.ecomind.users.interfaces.profile.TextLink

@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onSaved()
        }
    }

    EditProfileContent(
        state = state,
        onNameChange = viewModel::onNameChange,
        onSave = viewModel::save,
        onBack = onBack
    )
}

@Composable
private fun EditProfileContent(
    state: EditProfileUiState,
    onNameChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        EditProfileHeader(onBack = onBack)

        if (state.isLoading) {
            val loadingDescription = stringResource(R.string.loading)
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 48.dp)
                    .semantics { contentDescription = loadingDescription }
            )
        } else {
            InitialsAvatar(
                name = state.name,
                size = 76.dp,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            LabeledTextField(
                label = stringResource(R.string.edit_profile_name_label),
                value = state.name,
                onValueChange = onNameChange,
                errorMessage = state.nameError?.let { stringResource(it) },
                labelStyle = interTextStyle(14, FontWeight.Bold),
                labelColor = MaterialTheme.colorScheme.primary,
                placeholder = stringResource(R.string.edit_profile_name_placeholder),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        onSave()
                    }
                )
            )

            // The role is shown but cannot be changed from the app
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .padding(top = 24.dp, start = 16.dp, end = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.edit_profile_role_label),
                    style = interTextStyle(14),
                    color = EcoMindTheme.colors.textSecondary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = stringResource(
                        if (state.socialRole == SocialRole.PARENT) {
                            R.string.profile_role_parent
                        } else {
                            R.string.profile_role_student
                        }
                    ),
                    style = interTextStyle(14, FontWeight.Bold),
                    color = EcoMindTheme.colors.textPrimary
                )
            }

            if (state.saveFailed) {
                // The screen reader announces the failure as soon as it appears
                Column(
                    modifier = Modifier
                        .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                        .semantics { liveRegion = LiveRegionMode.Polite }
                ) {
                    Text(
                        text = stringResource(R.string.edit_profile_save_failed),
                        style = interTextStyle(14, FontWeight.Bold),
                        color = EcoMindTheme.colors.errorRed
                    )
                    Text(
                        text = stringResource(R.string.edit_profile_save_failed_hint),
                        style = interTextStyle(13),
                        color = EcoMindTheme.colors.errorRed,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
            // After a failure the same button retries with the text that is still in the field
            PillButton(
                text = stringResource(
                    if (state.saveFailed) R.string.profile_try_again else R.string.edit_profile_save
                ),
                onClick = onSave,
                faceColor = EcoGreen,
                baseColor = EcoGreenDark,
                height = 48.dp,
                textStyle = interTextStyle(16, FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            if (state.isSaving) {
                val savingDescription = stringResource(R.string.loading)
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 12.dp)
                        .semantics { contentDescription = savingDescription }
                )
            }
            if (!state.saveFailed) {
                TextLink(
                    text = stringResource(R.string.edit_profile_cancel),
                    onClick = onBack,
                    style = interTextStyle(13),
                    color = EcoMindTheme.colors.textSecondary,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

// Back arrow and title, shared with the share profile screen
@Composable
fun SubScreenHeader(
    title: String,
    onBack: () -> Unit
) {
    val backDescription = stringResource(R.string.back)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 16.dp)
    ) {
        TextLink(
            text = stringResource(R.string.profile_error_back),
            onClick = onBack,
            style = interTextStyle(24, FontWeight.Bold),
            // The screen reader says "Back" instead of reading the symbol
            modifier = Modifier
                .align(Alignment.CenterStart)
                .clearAndSetSemantics { contentDescription = backDescription }
        )
        Text(
            text = title,
            style = interTextStyle(20, FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.Center)
                .semantics { heading() }
        )
    }
}

@Composable
private fun EditProfileHeader(onBack: () -> Unit) {
    SubScreenHeader(title = stringResource(R.string.edit_profile_title), onBack = onBack)
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun EditProfileContentPreview() {
    EcoMindTheme {
        EditProfileContent(
            state = EditProfileUiState(
                isLoading = false,
                name = stringResource(R.string.app_name),
                socialRole = SocialRole.STUDENT
            ),
            onNameChange = {},
            onSave = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun EditProfileContentErrorsPreview() {
    EcoMindTheme {
        EditProfileContent(
            state = EditProfileUiState(
                isLoading = false,
                name = "",
                socialRole = SocialRole.STUDENT,
                nameError = R.string.edit_profile_name_required,
                saveFailed = true
            ),
            onNameChange = {},
            onSave = {},
            onBack = {}
        )
    }
}
