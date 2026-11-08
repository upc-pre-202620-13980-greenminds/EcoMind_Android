package pe.greenminds.ecomind.quests.presentation.ui.family

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.quests.domain.entity.Quest
import pe.greenminds.ecomind.quests.domain.valueobject.QuestCategory
import pe.greenminds.ecomind.quests.presentation.ui.QuestTile
import pe.greenminds.ecomind.shared.interfaces.theme.*

@Composable
internal fun FamilyQuestCard(
    quest: Quest,
    onView: () -> Unit,
    modifier: Modifier = Modifier,
    added: Boolean = false,
    busy: Boolean = false,
    onAdd: (() -> Unit)? = null,
    onRemove: (() -> Unit)? = null,
    onMoveUp: (() -> Unit)? = null,
    onMoveDown: (() -> Unit)? = null,
    showTile: Boolean = true
) {
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), color = White,
        shadowElevation = 2.dp, border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FamilyQuestChip(stringResource(when (quest.category) {
                    QuestCategory.ENERGY -> R.string.quest_category_energy
                    QuestCategory.WATER -> R.string.quest_category_water
                    QuestCategory.RECYCLE -> R.string.quest_category_recycle
                }), when (quest.category) {
                    QuestCategory.ENERGY -> SunYellow
                    QuestCategory.WATER -> SkyBlue
                    QuestCategory.RECYCLE -> LeafGreen
                })
                quest.estimatedMinutes?.let { FamilyQuestChip(stringResource(R.string.quest_chip_minutes, it), ChipGray) }
                FamilyQuestChip(stringResource(R.string.quest_chip_ecopoints, quest.reward.ecopoints), ChipGreen)
                FamilyQuestChip(stringResource(R.string.quest_theme_collaborative), ChipPurple)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showTile) {
                    QuestTile(quest.theme, quest.title, onView, Modifier.size(84.dp))
                    Spacer(Modifier.width(12.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(quest.title, style = interTextStyle(15, FontWeight.SemiBold), color = TextPrimary)
                    Text(quest.description, style = interTextStyle(12), color = DarkGrayText,
                        maxLines = 3, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 4.dp))
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FamilyPlanButton(stringResource(R.string.quest_view), onView, Modifier.weight(1f), blue = true, compact = true)
                if (onAdd != null) {
                    FamilyPlanButton(stringResource(if (added) R.string.family_plan_added else R.string.family_plan_add),
                        onAdd, Modifier.weight(1f), enabled = !added && !busy, compact = true)
                }
                if (onRemove != null) {
                    IconButton(onClick = onRemove, enabled = !busy) {
                        Icon(painterResource(R.drawable.ic_family_delete), stringResource(R.string.family_plan_remove),
                            modifier = Modifier.size(20.dp), tint = TextPrimary)
                    }
                    Spacer(Modifier.weight(0.2f))
                    IconButton(onClick = { onMoveUp?.invoke() }, enabled = onMoveUp != null && !busy) {
                        Icon(painterResource(R.drawable.ic_chevron_down), stringResource(R.string.family_plan_move_up), Modifier.rotate(180f))
                    }
                    IconButton(onClick = { onMoveDown?.invoke() }, enabled = onMoveDown != null && !busy) {
                        Icon(painterResource(R.drawable.ic_chevron_down), stringResource(R.string.family_plan_move_down))
                    }
                }
            }
        }
    }
}
