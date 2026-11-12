package pe.greenminds.ecomind.shared.interfaces.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.ErrorRed
import pe.greenminds.ecomind.shared.interfaces.theme.GemBlue
import pe.greenminds.ecomind.shared.interfaces.theme.White
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle

// Header of the main screens: logo, counters and shortcuts
@Composable
fun EcoMindTopBar(
    gemBalance: Int,
    streak: Int,
    onNotificationsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    // 0 hides the badge, so the screens that do not pass it look the same as before
    unreadNotifications: Int = 0
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(63.dp)
                .padding(start = 12.dp)
        ) {
            EcoMindLogo(modifier = Modifier.size(width = 100.dp, height = 36.dp))
            Spacer(modifier = Modifier.weight(1f))

            HeaderCounter(
                iconRes = R.drawable.ic_gem,
                iconWidth = 22.dp,
                iconHeight = 22.dp,
                value = gemBalance,
                color = GemBlue,
                description = stringResource(R.string.top_bar_gems, gemBalance)
            )
            Spacer(modifier = Modifier.width(6.dp))
            HeaderCounter(
                iconRes = R.drawable.ic_ecopoints,
                iconWidth = 30.dp,
                iconHeight = 26.dp,
                value = streak,
                color = MaterialTheme.colorScheme.primary,
                description = pluralStringResource(R.plurals.top_bar_streak, streak, streak)
            )

            IconButton(onClick = onNotificationsClick) {
                NotificationsBell(unreadCount = unreadNotifications)
            }
            IconButton(onClick = onSettingsClick) {
                Image(
                    painter = painterResource(R.drawable.ic_settings),
                    contentDescription = stringResource(R.string.top_bar_settings),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        HorizontalDivider(
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}

// Bell with the red badge of frame 63; the screen reader says "Notifications, 3 unread"
@Composable
private fun NotificationsBell(unreadCount: Int) {
    val description = if (unreadCount > 0) {
        pluralStringResource(R.plurals.top_bar_notifications_unread, unreadCount, unreadCount)
    } else {
        stringResource(R.string.top_bar_notifications)
    }

    Box {
        Image(
            painter = painterResource(R.drawable.ic_notifications),
            contentDescription = description,
            modifier = Modifier.size(24.dp)
        )
        if (unreadCount > 0) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 7.dp, y = (-5).dp)
                    .heightIn(min = 16.dp)
                    .widthIn(min = 15.dp)
                    .background(ErrorRed, RoundedCornerShape(8.dp))
                    .padding(horizontal = 4.dp)
                    .clearAndSetSemantics { }
            ) {
                Text(
                    text = if (unreadCount > MAX_BADGE_COUNT) {
                        "$MAX_BADGE_COUNT+"
                    } else {
                        "$unreadCount"
                    },
                    style = interTextStyle(10, FontWeight.Bold),
                    color = White
                )
            }
        }
    }
}

// Bigger numbers are shown as "99+" so the badge keeps its size
private const val MAX_BADGE_COUNT = 99

@Composable
private fun HeaderCounter(
    @DrawableRes iconRes: Int,
    iconWidth: Dp,
    iconHeight: Dp,
    value: Int,
    color: Color,
    description: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        // The screen reader says "360 gems" instead of reading icon and number apart
        modifier = Modifier.clearAndSetSemantics { contentDescription = description }
    ) {
        Image(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(width = iconWidth, height = iconHeight)
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleSmall,
            color = color,
            maxLines = 1
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun EcoMindTopBarPreview() {
    EcoMindTheme {
        EcoMindTopBar(
            gemBalance = 360,
            streak = 16,
            onNotificationsClick = {},
            onSettingsClick = {},
            unreadNotifications = 3
        )
    }
}
