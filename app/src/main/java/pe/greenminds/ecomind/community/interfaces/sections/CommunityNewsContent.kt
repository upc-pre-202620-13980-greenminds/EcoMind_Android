package pe.greenminds.ecomind.community.interfaces.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.theme.CardBorder
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreen
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.TextPrimary
import pe.greenminds.ecomind.shared.interfaces.theme.TextSecondary
import pe.greenminds.ecomind.shared.interfaces.theme.White
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle

@Composable
fun CommunityNewsContent(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        NewsCard(
            author = stringResource(R.string.community_news_author_minialex),
            message = stringResource(R.string.community_news_achievement_message),
            time = stringResource(R.string.community_news_time_now)
        )
        NewsCard(
            author = stringResource(R.string.community_news_author_tomiflash),
            message = stringResource(R.string.community_news_post_message),
            time = stringResource(R.string.community_news_time_recent)
        )
    }
}

@Composable
private fun NewsCard(
    author: String,
    message: String,
    time: String
) {
    val shape = RoundedCornerShape(28.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White, shape)
            .border(2.dp, CardBorder, shape)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = author,
                style = interTextStyle(14, FontWeight.SemiBold),
                color = TextPrimary,
                modifier = Modifier.padding(start = 10.dp)
            )
        }

        Text(
            text = message,
            style = interTextStyle(13),
            color = TextPrimary,
            modifier = Modifier.padding(top = 12.dp)
        )
        Text(
            text = time,
            style = interTextStyle(11),
            color = TextSecondary,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun CommunityNewsContentPreview() {
    EcoMindTheme {
        CommunityNewsContent(modifier = Modifier.padding(16.dp))
    }
}
