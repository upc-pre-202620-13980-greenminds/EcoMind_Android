package pe.greenminds.ecomind.quests.interfaces.search

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.quests.domain.entity.Quest
import pe.greenminds.ecomind.quests.presentation.ui.QuestMetadataChips
import pe.greenminds.ecomind.quests.presentation.ui.QuestTile
import pe.greenminds.ecomind.quests.presentation.ui.displayTheme
import pe.greenminds.ecomind.shared.interfaces.components.PillButton
import pe.greenminds.ecomind.shared.interfaces.theme.DividerGray
import pe.greenminds.ecomind.shared.interfaces.theme.SkyBlue
import pe.greenminds.ecomind.shared.interfaces.theme.SkyBlueDark
import pe.greenminds.ecomind.shared.interfaces.theme.TextPrimary
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle

@Composable
fun QuestSearchResultCard(
    quest: Quest,
    onOpen: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(8.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, DividerGray, shape)
            .clickable(role = Role.Button, onClick = onOpen)
            .padding(12.dp)
    ) {
        QuestMetadataChips(quest)

        Spacer(modifier = Modifier.height(8.dp))
        Row {
            QuestTile(
                theme = quest.displayTheme,
                contentDescription = quest.title,
                onClick = onOpen,
                modifier = Modifier.size(width = 93.dp, height = 90.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quest.title,
                    style = interTextStyle(15, FontWeight.SemiBold),
                    color = TextPrimary
                )
                Text(
                    text = quest.description,
                    style = interTextStyle(11, FontWeight.Medium),
                    color = TextPrimary,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                        .width(116.dp)
                ) {
                    PillButton(
                        text = stringResource(R.string.quest_view),
                        onClick = onOpen,
                        faceColor = SkyBlue,
                        baseColor = SkyBlueDark,
                        height = 32.dp,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

