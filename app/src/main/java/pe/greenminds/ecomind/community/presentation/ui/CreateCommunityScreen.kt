package pe.greenminds.ecomind.community.presentation.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.community.presentation.states.CommunityDirectoryTab
import pe.greenminds.ecomind.community.domain.valueobjects.CommunityFormField
import pe.greenminds.ecomind.community.presentation.states.CreateCommunityUiState
import pe.greenminds.ecomind.shared.interfaces.theme.poppinsTextStyle

private val CommunityFormGreen = Color(0xFF69A44E)
private val CommunityFormLightGreen = Color(0xFFE3F6D8)
private val CommunityFormGray = Color(0xFFEFEFEF)
private val CommunityFormRed = Color(0xFFE65D64)

@Composable
fun CreateCommunityScreen(
    state: CreateCommunityUiState,
    moreEnabled: Boolean,
    onChange: (CommunityFormField, String) -> Unit,
    onImageSelected: (String?) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    onLocalTab: () -> Unit
) {
    val enabled = !state.isSubmitting

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) onImageSelected(uri.toString())
    }

    BackHandler {
        if (enabled) onCancel()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp)
    ) {
        CommunitySelector(
            selectedTab = CommunityDirectoryTab.MORE,
            moreEnabled = moreEnabled,
            onLocalTab = {
                if (enabled) onLocalTab()
            },
            onMoreTab = {
                if (enabled) onCancel()
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = stringResource(R.string.community_form_title),
                style = poppinsTextStyle(24, FontWeight.SemiBold),
                color = CommunityFormGreen,
                modifier = Modifier.weight(1f)
            )

            Image(
                painter = painterResource(R.drawable.img_peeking),
                contentDescription = null,
                modifier = Modifier.size(width = 90.dp, height = 48.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, RoundedCornerShape(10.dp))
                .background(
                    CommunityFormLightGreen,
                    RoundedCornerShape(10.dp)
                )
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CommunityFormInput(
                label = stringResource(R.string.community_form_name),
                value = state.name,
                onValueChange = { onChange(CommunityFormField.NAME, it) },
                enabled = enabled
            )

            CommunityFormInput(
                label = stringResource(R.string.community_form_description),
                value = state.description,
                onValueChange = { onChange(CommunityFormField.DESCRIPTION, it) },
                enabled = enabled,
                singleLine = false,
                minHeight = 70
            )

            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                CommunityFormInput(
                    label = stringResource(R.string.community_form_topic),
                    value = state.topic,
                    onValueChange = { onChange(CommunityFormField.TOPIC, it) },
                    enabled = enabled,
                    modifier = Modifier.weight(1.3f)
                )

                CommunityFormInput(
                    label = stringResource(R.string.community_form_limit),
                    value = state.limit,
                    onValueChange = { onChange(CommunityFormField.LIMIT, it) },
                    enabled = enabled,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(stringResource(R.string.community_form_icon))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(154.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CommunityFormGray)
                    .border(1.dp, Color.Gray, RoundedCornerShape(10.dp))
                    .clickable(
                        enabled = enabled,
                        role = Role.Button
                    ) {
                        photoPicker.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }
            ) {
                if (state.imageUri != null) {
                    AsyncImage(
                        model = state.imageUri,
                        contentDescription = stringResource(
                            R.string.community_form_icon
                        ),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Icon(
                    imageVector = Icons.Default.AttachFile,
                    contentDescription = stringResource(
                        R.string.community_form_choose_icon
                    ),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                )
            }

            if (state.imageUri != null) {
                TextButton(
                    onClick = { onImageSelected(null) },
                    enabled = enabled
                ) {
                    Text(stringResource(R.string.community_form_remove_icon))
                }
            }

            Text(
                text = stringResource(R.string.community_form_image_hint),
                style = MaterialTheme.typography.bodySmall
            )

            val errorText = state.errorMessage
                ?: state.errorRes?.let { stringResource(it) }

            errorText?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            state.permissionError?.let {
                Text(
                    text = stringResource(R.string.community_form_permission_error),
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (state.isSubmitting || state.isCheckingPermission) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }

            Row(
                modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                EventFlowButton(
                    text = stringResource(R.string.community_create),
                    onClick = onSubmit,
                    enabled = enabled &&
                            state.canCreate &&
                            !state.isCheckingPermission,
                    modifier = Modifier.weight(2f),
                    color = CommunityFormGreen
                )

                EventFlowButton(
                    text = stringResource(R.string.event_flow_cancel),
                    onClick = onCancel,
                    enabled = enabled,
                    modifier = Modifier.weight(1f),
                    color = CommunityFormRed
                )
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun CommunityFormInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minHeight: Int = 36,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(text = label)

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = singleLine,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = Color.Black
            ),
            cursorBrush = SolidColor(CommunityFormGreen),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = minHeight.dp)
                .background(CommunityFormGray, RoundedCornerShape(10.dp))
                .border(1.dp, Color.Gray, RoundedCornerShape(10.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        )
    }
}