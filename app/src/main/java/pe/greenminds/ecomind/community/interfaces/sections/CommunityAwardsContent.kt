package pe.greenminds.ecomind.community.interfaces.sections

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.theme.CardBorder
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.SunYellow
import pe.greenminds.ecomind.shared.interfaces.theme.TextPrimary
import pe.greenminds.ecomind.shared.interfaces.theme.White
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle

@Composable
fun CommunityAwardsContent(modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        AwardCard(
            title = stringResource(R.string.community_award_achievement_title),
            description = stringResource(R.string.community_award_achievement_description)
        )
        AwardCard(
            title = stringResource(R.string.community_award_family_title),
            description = stringResource(R.string.community_award_family_description)
        )
        AwardCard(
            title = stringResource(R.string.community_award_member_title),
            description = stringResource(R.string.community_award_member_description)
        )
        AwardCard(
            title = stringResource(R.string.community_award_goal_title),
            description = stringResource(R.string.community_award_goal_description)
        )
    }
}

@Composable
private fun AwardCard(
    title: String,
    description: String
) {
    val shape = RoundedCornerShape(28.dp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(White, shape)
            .border(2.dp, CardBorder, shape)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .background(SunYellow.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
        ) {
            Image(
                painter = painterResource(R.drawable.img_trophy),
                contentDescription = null,
                modifier = Modifier.size(40.dp)
            )
        }

        Column(modifier = Modifier.padding(start = 14.dp)) {
            Text(
                text = title,
                style = interTextStyle(14, FontWeight.Medium),
                color = TextPrimary
            )
            Text(
                text = description,
                style = interTextStyle(12),
                color = TextPrimary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun CommunityAwardsContentPreview() {
    EcoMindTheme {
        CommunityAwardsContent(modifier = Modifier.padding(16.dp))
    }
}
