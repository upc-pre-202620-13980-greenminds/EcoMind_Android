package pe.greenminds.ecomind.users.interfaces.commitment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.components.PillButton
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreen
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreenDark
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle
import pe.greenminds.ecomind.users.domain.model.PersonalCommitment
import pe.greenminds.ecomind.users.interfaces.editprofile.SubScreenHeader
import pe.greenminds.ecomind.users.interfaces.profile.TextLink

// Frames 05 and 06 only differ in the title and in whether the field starts empty
@Composable
fun CommitmentScreen(
    isEditing: Boolean,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: CommitmentViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onSaved()
        }
    }

    CommitmentContent(
        isEditing = isEditing,
        state = state,
        onTextChange = viewModel::onTextChange,
        onSave = viewModel::save,
        onBack = onBack
    )
}

@Composable
private fun CommitmentContent(
    isEditing: Boolean,
    state: CommitmentUiState,
    onTextChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
    ) {
        SubScreenHeader(
            title = stringResource(
                if (isEditing) R.string.commitment_edit_title else R.string.commitment_add_title
            ),
            onBack = onBack
        )

        if (state.isLoading) {
            val loadingDescription = stringResource(R.string.loading)
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 48.dp)
                    .semantics { contentDescription = loadingDescription }
            )
        } else {
            // The form scrolls and the buttons stay at the bottom, as in the design
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = stringResource(R.string.commitment_subtitle),
                    style = interTextStyle(13),
                    color = EcoMindTheme.colors.textSecondary
                )

                Spacer(modifier = Modifier.height(32.dp))
                CommitmentField(
                    text = state.text,
                    onTextChange = onTextChange,
                    errorMessage = state.textError?.let {
                        stringResource(it, PersonalCommitment.MAX_LENGTH)
                    }
                )

                if (state.saveFailed) {
                    SaveFailureBox(modifier = Modifier.padding(top = 24.dp))
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // After a failure the same button retries with the text that is still in the field
            PillButton(
                text = stringResource(
                    if (state.saveFailed) R.string.commitment_retry else R.string.commitment_save
                ),
                onClick = onSave,
                faceColor = EcoGreen,
                baseColor = EcoGreenDark,
                height = 48.dp,
                textStyle = interTextStyle(16, FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            TextLink(
                text = stringResource(R.string.commitment_cancel),
                onClick = onBack,
                style = interTextStyle(13),
                color = EcoMindTheme.colors.textSecondary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CommitmentField(
    text: String,
    onTextChange: (String) -> Unit,
    errorMessage: String?
) {
    val label = stringResource(R.string.commitment_label)
    val counterDescription = stringResource(
        R.string.commitment_counter_description,
        text.length,
        PersonalCommitment.MAX_LENGTH
    )

    Text(
        text = label,
        style = interTextStyle(14, FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(14.dp))

    BasicTextField(
        value = text,
        onValueChange = onTextChange,
        textStyle = interTextStyle(14).copy(color = EcoMindTheme.colors.textPrimary),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        // The screen reader announces the label and, if there is one, the error
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = label
                if (errorMessage != null) error(errorMessage)
            },
        decorationBox = { innerTextField ->
            // heightIn instead of height, so a bigger system font still fits
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 154.dp)
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp))
                    .border(
                        width = 1.dp,
                        color = if (errorMessage != null) {
                            EcoMindTheme.colors.requiredRed
                        } else {
                            EcoMindTheme.colors.fieldBorder
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 15.dp)
            ) {
                if (text.isEmpty()) {
                    Text(
                        text = stringResource(R.string.commitment_placeholder),
                        style = interTextStyle(14),
                        color = EcoMindTheme.colors.placeholder
                    )
                }
                innerTextField()
            }
        }
    )

    // Error on the left and counter on the right, under the field
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 13.dp)
    ) {
        Text(
            text = errorMessage.orEmpty(),
            style = interTextStyle(13),
            color = EcoMindTheme.colors.requiredRed,
            modifier = Modifier
                .weight(1f)
                .semantics { liveRegion = LiveRegionMode.Polite }
        )
        Text(
            text = stringResource(
                R.string.commitment_counter,
                text.length,
                PersonalCommitment.MAX_LENGTH
            ),
            style = interTextStyle(12),
            color = EcoMindTheme.colors.textSecondary,
            // "42 of 150 characters" is read instead of "42/150"
            modifier = Modifier
                .padding(start = 8.dp)
                .clearAndSetSemantics { contentDescription = counterDescription }
        )
    }
}

@Composable
private fun SaveFailureBox(modifier: Modifier = Modifier) {
    // The screen reader announces the failure as soon as it appears
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(EcoMindTheme.colors.errorSurface, RoundedCornerShape(12.dp))
            .padding(horizontal = 15.dp, vertical = 16.dp)
            .semantics { liveRegion = LiveRegionMode.Polite }
    ) {
        Text(
            text = stringResource(R.string.commitment_save_failed),
            style = interTextStyle(14, FontWeight.Bold),
            color = EcoMindTheme.colors.errorText
        )
        Text(
            text = stringResource(R.string.commitment_save_failed_hint),
            style = interTextStyle(13),
            color = EcoMindTheme.colors.errorText,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun AddCommitmentPreview() {
    EcoMindTheme {
        CommitmentContent(
            isEditing = false,
            state = CommitmentUiState(isLoading = false),
            onTextChange = {},
            onSave = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun CommitmentRequiredPreview() {
    EcoMindTheme {
        CommitmentContent(
            isEditing = false,
            state = CommitmentUiState(
                isLoading = false,
                textError = R.string.commitment_required
            ),
            onTextChange = {},
            onSave = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun CommitmentSaveFailurePreview() {
    EcoMindTheme {
        CommitmentContent(
            isEditing = true,
            state = CommitmentUiState(
                isLoading = false,
                text = stringResource(R.string.commitment_placeholder),
                saveFailed = true
            ),
            onTextChange = {},
            onSave = {},
            onBack = {}
        )
    }
}
