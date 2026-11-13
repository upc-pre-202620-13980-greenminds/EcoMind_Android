package pe.greenminds.ecomind.users.interfaces.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.monetization.domain.model.EquippedCosmetics
import pe.greenminds.ecomind.monetization.interfaces.store.cosmeticImageResource
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

// Row of a friend or a family member; it ends with a label such as "You", or with an arrow
// when the row opens another screen
@Composable
fun PersonRow(
    name: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailingLabel: String? = null,
    avatarImageReference: String? = null,
    onClick: (() -> Unit)? = null
) {
    // Same catalog of images as the avatar of the header; 0 means the reference is unknown
    val avatarResource = cosmeticImageResource(avatarImageReference, fallback = 0)

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
            if (avatarResource == 0) {
                InitialsAvatar(name = name)
            } else {
                // Decorative: the name is written next to the avatar
                Image(
                    painter = painterResource(avatarResource),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(44.dp)
                )
            }
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
            } else if (onClick != null) {
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

// Avatar of the profile header with the equipped cosmetics drawn on top, by slot.
// Share profile shows it smaller; the layers keep the proportions of the 110dp header
@Composable
internal fun ProfileAvatar(
    name: String,
    equippedCosmetics: EquippedCosmetics,
    size: Dp = 110.dp
) {
    val avatar = equippedCosmetics.avatar
    val scale = size / 110.dp

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size)
    ) {
        if (avatar == null) {
            InitialsAvatar(name = name, size = 76.dp * scale)
        } else {
            Image(
                painter = painterResource(cosmeticImageResource(avatar.imageReference)),
                contentDescription = avatar.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(76.dp * scale)
            )
        }

        equippedCosmetics.body?.let { cosmetic ->
            Image(
                painter = painterResource(cosmeticImageResource(cosmetic.imageReference)),
                contentDescription = cosmetic.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(76.dp * scale)
            )
        }

        equippedCosmetics.head?.let { cosmetic ->
            Image(
                painter = painterResource(cosmeticImageResource(cosmetic.imageReference)),
                contentDescription = cosmetic.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset { IntOffset(0, -(4.dp * scale).roundToPx()) }
                    .size(44.dp * scale)
            )
        }

        equippedCosmetics.accessory?.let { cosmetic ->
            Image(
                painter = painterResource(cosmeticImageResource(cosmetic.imageReference)),
                contentDescription = cosmetic.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(34.dp * scale)
            )
        }
    }
}
