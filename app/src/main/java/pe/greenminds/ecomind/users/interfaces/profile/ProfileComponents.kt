package pe.greenminds.ecomind.users.interfaces.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.components.InitialsAvatar
import pe.greenminds.ecomind.shared.interfaces.theme.RowDivider
import pe.greenminds.ecomind.shared.interfaces.theme.TextPrimary
import pe.greenminds.ecomind.shared.interfaces.theme.TextSecondary
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle

// Text that works as a link; the box around it keeps a touch target of 48dp
@Composable
fun TextLink(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: TextStyle = interTextStyle(13, FontWeight.Bold),
    color: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .clickable(role = Role.Button, onClick = onClick)
    ) {
        Text(text = text, style = style, color = color)
    }
}

// Row of a friend or a family member; it ends with a label such as "You" or with an arrow
@Composable
fun PersonRow(
    name: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailingLabel: String? = null,
    onClick: (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .then(
                    if (onClick != null) {
                        Modifier.clickable(role = Role.Button, onClick = onClick)
                    } else {
                        Modifier
                    }
                )
        ) {
            InitialsAvatar(name = name)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = interTextStyle(14, FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = interTextStyle(12),
                    color = TextSecondary
                )
            }
            if (trailingLabel != null) {
                Text(
                    text = trailingLabel,
                    style = interTextStyle(13, FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            } else {
                // The arrow is decorative, so the screen reader skips it
                Text(
                    text = stringResource(R.string.profile_row_arrow),
                    style = interTextStyle(13, FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .clearAndSetSemantics { }
                )
            }
        }
        HorizontalDivider(thickness = 1.dp, color = RowDivider)
    }
}

// Centered title and message for the empty states of the tabs
@Composable
fun EmptyTabMessage(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    titleSizeSp: Int = 18
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 32.dp)
    ) {
        Text(
            text = title,
            style = interTextStyle(titleSizeSp, FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = message,
            style = interTextStyle(13),
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp, start = 24.dp, end = 24.dp)
        )
    }
}

@Composable
fun TabLoading(modifier: Modifier = Modifier) {
    val loadingDescription = stringResource(R.string.loading)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 48.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.semantics { contentDescription = loadingDescription }
        )
    }
}

// The design has no error state for a single tab, so this one is kept minimal
@Composable
fun TabError(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 32.dp)
    ) {
        Text(
            text = stringResource(R.string.profile_tab_error),
            style = interTextStyle(13),
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
        )
        TextLink(
            text = stringResource(R.string.profile_try_again),
            onClick = onRetry
        )
    }
}
