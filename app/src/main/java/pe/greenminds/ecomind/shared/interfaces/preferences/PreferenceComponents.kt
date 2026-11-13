package pe.greenminds.ecomind.shared.interfaces.preferences

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.White
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle

// Back symbol and green title of the screens of Settings
@Composable
fun PreferencesHeader(
    title: String,
    onBack: () -> Unit
) {
    val backDescription = stringResource(R.string.back)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                .clickable(role = Role.Button, onClick = onBack)
                // The screen reader says "Back" instead of reading the symbol
                .clearAndSetSemantics { contentDescription = backDescription }
        ) {
            Text(
                text = stringResource(R.string.settings_back_symbol),
                style = interTextStyle(24, FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = title,
            style = interTextStyle(20, FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 48.dp)
                .semantics { heading() }
        )
    }
}

@Composable
fun PreferencesDescription(text: String) {
    Text(
        text = text,
        style = interTextStyle(13),
        color = EcoMindTheme.colors.textSecondary,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
fun PreferencesSectionTitle(text: String) {
    Text(
        text = text,
        style = interTextStyle(16, FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .padding(top = 18.dp, bottom = 11.dp)
            .semantics { heading() }
    )
}

// Light card with a thin border that groups the rows
@Composable
fun PreferencesGroup(content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(12.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(EcoMindTheme.colors.surfaceTint)
            .border(1.dp, EcoMindTheme.colors.cardBorder, shape),
        content = content
    )
}

@Composable
fun PreferencesDivider() {
    HorizontalDivider(thickness = 1.dp, color = EcoMindTheme.colors.rowDivider)
}

// Row that opens another screen: title, current value and an arrow (frame 69)
@Composable
fun NavigationRow(
    title: String,
    onClick: () -> Unit,
    value: String? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = title,
            style = interTextStyle(14),
            color = EcoMindTheme.colors.textPrimary,
            modifier = Modifier.weight(1f)
        )
        if (value != null) {
            Text(
                text = value,
                style = interTextStyle(13),
                color = EcoMindTheme.colors.textSecondary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        // The arrow is decorative, so the screen reader skips it
        Text(
            text = stringResource(R.string.settings_row_arrow),
            style = interTextStyle(13, FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(start = 8.dp)
                .clearAndSetSemantics { }
        )
    }
}

// Option of frames 73 and 74: the whole row selects it
@Composable
fun RadioOptionRow(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onSelect)
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = label,
            style = interTextStyle(14, if (selected) FontWeight.Bold else FontWeight.Normal),
            color = EcoMindTheme.colors.textPrimary,
            modifier = Modifier.weight(1f)
        )
        // The row already handles the click, so the radio button only shows the state
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = MaterialTheme.colorScheme.primary,
                unselectedColor = EcoMindTheme.colors.radioBorder
            )
        )
    }
}

// Category of frame 70: title, description and a switch; the whole row toggles it
@Composable
fun SwitchRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 59.dp)
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = interTextStyle(14, FontWeight.Bold),
                color = EcoMindTheme.colors.textPrimary
            )
            Text(
                text = description,
                style = interTextStyle(12),
                color = EcoMindTheme.colors.textSecondary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = null,
            colors = SwitchDefaults.colors(
                checkedThumbColor = White,
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = White,
                uncheckedTrackColor = EcoMindTheme.colors.cardBorder,
                uncheckedBorderColor = EcoMindTheme.colors.cardBorder
            ),
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
fun PreferencesSaveError(text: String) {
    Text(
        text = text,
        style = interTextStyle(13),
        color = EcoMindTheme.colors.errorRed,
        modifier = Modifier
            .padding(top = 12.dp)
            .semantics { liveRegion = LiveRegionMode.Polite }
    )
}

// From Android 13 the alerts of the phone need the permission of the user (TS-008).
// It is asked when the inbox or the notification preferences open, until the user answers.
@Composable
fun RequestNotificationPermission() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
