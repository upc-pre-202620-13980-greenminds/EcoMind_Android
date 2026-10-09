package pe.greenminds.ecomind.community.interfaces.events

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.community.domain.model.CommunityEvent
import pe.greenminds.ecomind.shared.interfaces.theme.DividerGray
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.MutedGray
import pe.greenminds.ecomind.shared.interfaces.theme.TextPrimary
import pe.greenminds.ecomind.shared.interfaces.theme.White
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle
import pe.greenminds.ecomind.shared.interfaces.theme.poppinsTextStyle

@Composable
fun CommunityEventCard(
    event: CommunityEvent,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(20.dp)
    val pillShape = RoundedCornerShape(percent = 50)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(White, cardShape)
            .border(2.dp, DividerGray, cardShape)
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)
    ) {
        Text(
            text = event.title,
            style = interTextStyle(18, FontWeight.Medium),
            color = TextPrimary
        )

        Row(modifier = Modifier.padding(top = 4.dp)) {
            Text(
                text = event.description,
                style = interTextStyle(12, FontWeight.Medium),
                color = TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(width = 97.dp, height = 65.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                        RoundedCornerShape(18.dp)
                    )
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .clip(pillShape)
                    .clickable(role = Role.Button, onClick = onAction)
            ) {
                Text(
                    text = stringResource(R.string.community_join_event),
                    style = poppinsTextStyle(12, FontWeight.Medium),
                    color = White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary, pillShape)
                        .padding(vertical = 6.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                    .clip(pillShape)
                    .clickable(role = Role.Button, onClick = onAction)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(width = 40.dp, height = 30.dp)
                        .background(MutedGray, pillShape)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = stringResource(R.string.community_dismiss_event),
                        tint = White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun CommunityEventCardPreview() {
    EcoMindTheme {
        CommunityEventCard(
            event = CommunityEvent(
                id = 1L,
                title = stringResource(R.string.app_name),
                description = stringResource(R.string.coming_soon_message),
                date = ""
            ),
            onAction = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
