package pe.greenminds.ecomind.shared.interfaces.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.GemBlue

// Header of the main screens: logo, balances and shortcuts
@Composable
fun EcoMindTopBar(
    gemBalance: Int,
    ecopoints: Int,
    onNotificationsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    unreadCount: Int = 0
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

            BalanceCounter(
                iconRes = R.drawable.ic_gem,
                iconWidth = 22.dp,
                iconHeight = 22.dp,
                value = gemBalance,
                color = GemBlue,
                description = stringResource(R.string.top_bar_gems, gemBalance)
            )
            Spacer(modifier = Modifier.width(6.dp))
            BalanceCounter(
                iconRes = R.drawable.ic_ecopoints,
                iconWidth = 30.dp,
                iconHeight = 26.dp,
                value = ecopoints,
                color = MaterialTheme.colorScheme.primary,
                description = stringResource(R.string.top_bar_ecopoints, ecopoints)
            )

            IconButton(onClick = onNotificationsClick) {
                BadgedBox(badge = { if (unreadCount > 0) Badge { Text(unreadCount.toString()) } }) {
                    Image(
                        painter = painterResource(R.drawable.ic_notifications),
                        contentDescription = stringResource(R.string.top_bar_notifications),
                        modifier = Modifier.size(24.dp)
                    )
                }
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

@Composable
private fun BalanceCounter(
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
            ecopoints = 16,
            onNotificationsClick = {},
            onSettingsClick = {}
        )
    }
}
