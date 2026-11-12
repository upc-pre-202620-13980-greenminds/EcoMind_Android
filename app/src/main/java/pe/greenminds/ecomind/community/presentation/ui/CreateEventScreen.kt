package pe.greenminds.ecomind.community.presentation.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.community.domain.valueobjects.CommunityType
import pe.greenminds.ecomind.community.presentation.states.CreateEventUiState
import pe.greenminds.ecomind.community.presentation.states.EventFormField
import pe.greenminds.ecomind.community.presentation.ui.map.EventLocationMap
import pe.greenminds.ecomind.shared.interfaces.theme.poppinsTextStyle

private val EventGreen = Color(0xFF69A44E)
private val EventCardGreen = Color(0xFFE3F6D8)
private val EventFieldGray = Color(0xFFEFEFEF)
private val EventRed = Color(0xFFE65D64)
private val EventFieldShape = RoundedCornerShape(10.dp)

@Composable
fun CreateEventScreen(
    state: CreateEventUiState,
    moreEnabled: Boolean,
    onChange: (EventFormField, String) -> Unit,
    onImageSelected: (String?) -> Unit,
    onPointSelected: (Double, Double) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit,
    onLocalTab: () -> Unit,
    onMoreTab: () -> Unit
) {
    val community = state.community ?: return
    val context = LocalContext.current
    val enabled = !state.isSubmitting

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) {
        if (it != null) onImageSelected(it.toString())
    }

    BackHandler {
        if (enabled) onCancel()
    }

    fun selectDate() {
        val initial = runCatching {
            LocalDate.parse(state.date)
        }.getOrDefault(LocalDate.now())

        DatePickerDialog(
            context,
            { _, year, month, day ->
                onChange(
                    EventFormField.DATE,
                    LocalDate.of(year, month + 1, day).toString()
                )
            },
            initial.year,
            initial.monthValue - 1,
            initial.dayOfMonth
        ).show()
    }

    fun selectTime() {
        val initial = runCatching {
            LocalTime.parse(state.time)
        }.getOrDefault(LocalTime.now())

        TimePickerDialog(
            context,
            { _, hour, minute ->
                onChange(
                    EventFormField.TIME,
                    String.format(Locale.US, "%02d:%02d", hour, minute)
                )
            },
            initial.hour,
            initial.minute,
            true
        ).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp)
    ) {
        EventCommunitySelector(
            localSelected = community.type == CommunityType.LOCAL,
            enabled = enabled,
            moreEnabled = moreEnabled,
            onLocalTab = onLocalTab,
            onMoreTab = onMoreTab
        )

        Text(
            text = community.name,
            style = poppinsTextStyle(24, FontWeight.SemiBold),
            color = EventGreen,
            modifier = Modifier.padding(start = 12.dp, top = 18.dp)
        )

        Spacer(Modifier.height(58.dp))

        Box(Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(R.drawable.img_peeking),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-30).dp, y = (-38).dp)
                    .size(width = 100.dp, height = 48.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(10.dp))
                    .background(EventCardGreen, RoundedCornerShape(10.dp))
                    .padding(
                        start = 12.dp,
                        end = 12.dp,
                        top = 22.dp,
                        bottom = 22.dp
                    ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    EventInput(
                        label = stringResource(R.string.event_name),
                        value = state.name,
                        onValueChange = {
                            onChange(EventFormField.NAME, it)
                        },
                        enabled = enabled,
                        modifier = Modifier.weight(3.4f)
                    )

                    EventInput(
                        label = stringResource(R.string.event_limit),
                        value = state.capacity,
                        onValueChange = {
                            onChange(EventFormField.CAPACITY, it)
                        },
                        enabled = enabled,
                        keyboard = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                }

                EventInput(
                    label = stringResource(R.string.event_description),
                    value = state.description,
                    onValueChange = {
                        onChange(EventFormField.DESCRIPTION, it)
                    },
                    enabled = enabled,
                    multiline = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    EventActionField(
                        label = stringResource(R.string.event_date),
                        value = state.date,
                        enabled = enabled,
                        onClick = ::selectDate,
                        modifier = Modifier.weight(1f)
                    )

                    EventActionField(
                        label = stringResource(R.string.event_time),
                        value = state.time,
                        enabled = enabled,
                        onClick = ::selectTime,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    EventInput(
                        label = stringResource(R.string.event_address),
                        value = state.address,
                        onValueChange = {
                            onChange(EventFormField.ADDRESS, it)
                        },
                        enabled = enabled,
                        modifier = Modifier.weight(1f)
                    )

                    Column(Modifier.weight(1f)) {
                        EventLabel(stringResource(R.string.event_icon))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 32.dp)
                                .background(EventFieldGray, EventFieldShape)
                                .border(1.dp, Color.Gray, EventFieldShape)
                                .clip(EventFieldShape),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
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
                                    .padding(horizontal = 7.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AttachFile,
                                    contentDescription = stringResource(
                                        R.string.event_attach
                                    ),
                                    tint = Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )

                                if (state.imageUri != null) {
                                    Text(
                                        stringResource(R.string.event_image_selected),
                                        style = poppinsTextStyle(10),
                                        color = Color.Black,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            if (state.imageUri != null) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = stringResource(
                                        R.string.event_remove_image
                                    ),
                                    tint = Color.Black,
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clickable(
                                            enabled = enabled,
                                            role = Role.Button
                                        ) {
                                            onImageSelected(null)
                                        }
                                        .padding(5.dp)
                                )
                            }
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    EventInput(
                        label = stringResource(R.string.event_latitude),
                        value = state.latitude,
                        onValueChange = {
                            onChange(EventFormField.LATITUDE, it)
                        },
                        enabled = enabled,
                        modifier = Modifier.weight(1f)
                    )

                    EventInput(
                        label = stringResource(R.string.event_longitude),
                        value = state.longitude,
                        onValueChange = {
                            onChange(EventFormField.LONGITUDE, it)
                        },
                        enabled = enabled,
                        modifier = Modifier.weight(1f)
                    )
                }

                EventLocationMap(
                    latitude = state.latitude,
                    longitude = state.longitude,
                    enabled = enabled,
                    onPointSelected = onPointSelected,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .fillMaxWidth()
                        .height(144.dp)
                        .border(1.dp, Color.White)
                )

                val error = state.errorRes?.let {
                    stringResource(it)
                } ?: state.errorMessage

                if (!error.isNullOrBlank()) {
                    Text(
                        error,
                        color = Color(0xFFB3261E),
                        style = poppinsTextStyle(12)
                    )
                }

                if (state.permissionError != null) {
                    Text(
                        stringResource(R.string.event_permission_failed),
                        color = Color(0xFFB3261E),
                        style = poppinsTextStyle(12)
                    )
                } else if (!state.canCreate && !state.isCheckingPermission) {
                    Text(
                        stringResource(R.string.event_parent_required),
                        color = Color(0xFFB3261E),
                        style = poppinsTextStyle(12)
                    )
                }

                Row(
                    modifier = Modifier.padding(
                        horizontal = 12.dp,
                        vertical = 4.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EventButton(
                        text = stringResource(
                            if (state.isSubmitting) {
                                R.string.event_saving
                            } else {
                                R.string.event_create
                            }
                        ),
                        color = EventGreen,
                        enabled = enabled &&
                                state.canCreate &&
                                !state.isCheckingPermission,
                        onClick = onSubmit,
                        modifier = Modifier.weight(2f)
                    )

                    EventButton(
                        text = stringResource(R.string.event_cancel),
                        color = EventRed,
                        enabled = enabled,
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (state.isSubmitting || state.isCheckingPermission) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(22.dp)
                            .align(Alignment.CenterHorizontally),
                        color = EventGreen,
                        strokeWidth = 2.dp
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun EventLabel(text: String) {
    Text(
        text,
        color = Color.Black,
        style = poppinsTextStyle(13, FontWeight.Medium),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(start = 2.dp, bottom = 2.dp)
    )
}

@Composable
private fun EventInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    multiline: Boolean = false,
    keyboard: KeyboardType = KeyboardType.Text
) {
    Column(modifier) {
        EventLabel(label)

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = !multiline,
            textStyle = poppinsTextStyle(13).copy(color = Color.Black),
            cursorBrush = SolidColor(EventGreen),
            keyboardOptions = KeyboardOptions(keyboardType = keyboard),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = if (multiline) 70.dp else 32.dp)
                .background(EventFieldGray, EventFieldShape)
                .border(1.dp, Color.Gray, EventFieldShape)
                .padding(horizontal = 9.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun EventActionField(
    label: String,
    value: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        EventLabel(label)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 32.dp)
                .background(EventFieldGray, EventFieldShape)
                .border(1.dp, Color.Gray, EventFieldShape)
                .clip(EventFieldShape)
                .clickable(
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick
                )
                .padding(horizontal = 8.dp, vertical = 5.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                value,
                color = Color.Black,
                style = poppinsTextStyle(13),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun EventButton(
    text: String,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(50)

    Box(
        modifier = modifier
            .heightIn(min = 32.dp)
            .clip(shape)
            .background(
                if (enabled) color else color.copy(alpha = 0.55f)
            )
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 4.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = Color.White,
            style = poppinsTextStyle(12),
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun EventCommunitySelector(
    localSelected: Boolean,
    enabled: Boolean,
    moreEnabled: Boolean,
    onLocalTab: () -> Unit,
    onMoreTab: () -> Unit
) {
    val shape = RoundedCornerShape(50)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .shadow(4.dp, shape)
            .background(Color.White, shape)
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf(
            Triple(R.string.event_local_tab, localSelected, onLocalTab),
            Triple(R.string.event_more_tab, !localSelected, onMoreTab)
        ).forEachIndexed { index, item ->
            val canSelect = enabled && (index == 0 || moreEnabled)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 30.dp)
                    .clip(shape)
                    .background(
                        if (item.second) EventGreen else Color.White
                    )
                    .clickable(
                        enabled = canSelect,
                        role = Role.Tab,
                        onClick = item.third
                    )
                    .padding(vertical = 5.dp, horizontal = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(item.first),
                    color = if (item.second) {
                        Color.White
                    } else {
                        Color(0xFF839E76)
                    },
                    style = poppinsTextStyle(12, FontWeight.SemiBold),
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}