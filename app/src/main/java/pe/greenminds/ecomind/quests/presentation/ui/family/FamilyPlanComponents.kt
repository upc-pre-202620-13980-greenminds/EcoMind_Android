package pe.greenminds.ecomind.quests.presentation.ui.family

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.quests.domain.valueobject.FamilyPlanStatus
import pe.greenminds.ecomind.quests.presentation.ui.invitations.PartyBackButton
import pe.greenminds.ecomind.shared.interfaces.components.PillButton
import pe.greenminds.ecomind.shared.interfaces.theme.*
import kotlin.math.roundToInt

@Composable
internal fun FamilyPlanButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    secondary: Boolean = false,
    blue: Boolean = false,
    compact: Boolean = false
) {
    val height = if (compact) 48.dp else 58.dp
    Box(modifier.heightIn(min = height), contentAlignment = Alignment.Center) {
        PillButton(
            text = text, onClick = onClick, enabled = enabled,
            faceColor = if (!enabled || secondary) MutedGray else if (blue) SkyBlue else EcoGreen,
            baseColor = if (!enabled || secondary) DarkGrayText else if (blue) SkyBlueDark else EcoGreenDark,
            height = height, textStyle = interTextStyle(if (compact) 15 else 20, FontWeight.SemiBold)
        )
    }
}

@Composable
internal fun FamilyPlanHeader(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(end = 20.dp), verticalAlignment = Alignment.CenterVertically) {
        PartyBackButton(onBack)
        Text(title, style = interTextStyle(16, FontWeight.SemiBold), modifier = Modifier.weight(1f))
    }
}

@Composable
internal fun FamilyPlanError(onRetry: () -> Unit, enabled: Boolean) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Text(stringResource(R.string.family_plan_error), style = interTextStyle(13), color = MaterialTheme.colorScheme.error)
        TextButton(onClick = onRetry, enabled = enabled) { Text(stringResource(R.string.quest_try_again)) }
    }
}

@Composable
internal fun FamilyPlanMessage(text: String, loading: Boolean = false) {
    Column(
        Modifier.fillMaxWidth().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (loading) CircularProgressIndicator(Modifier.size(24.dp), color = EcoGreen, strokeWidth = 2.dp)
        Text(text, style = interTextStyle(14), color = DarkGrayText)
    }
}

@Composable
internal fun FamilyPlanProgress(progress: Double, modifier: Modifier = Modifier) {
    val fraction = if (progress.isFinite()) (progress / 100.0).toFloat().coerceIn(0f, 1f) else 0f
    Column(modifier) {
        LinearProgressIndicator(
            progress = { fraction }, color = EcoGreen, trackColor = FieldGray,
            modifier = Modifier.fillMaxWidth().height(12.dp), strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
            drawStopIndicator = {}
        )
        Text(
            stringResource(R.string.family_plan_progress, (fraction * 100).roundToInt()),
            style = interTextStyle(12), color = DarkGrayText,
            modifier = Modifier.align(Alignment.End).padding(top = 6.dp)
        )
    }
}

@Composable
internal fun familyPlanStatus(status: FamilyPlanStatus): String = stringResource(when (status) {
    FamilyPlanStatus.DRAFT -> R.string.family_plan_draft
    FamilyPlanStatus.ACTIVE -> R.string.family_plan_active
    FamilyPlanStatus.COMPLETED -> R.string.family_plan_completed
    FamilyPlanStatus.CANCELLED -> R.string.family_plan_cancelled
})

@Composable
internal fun FamilyQuestChip(text: String, color: Color) {
    Text(text, color = White, style = interTextStyle(10, FontWeight.SemiBold), maxLines = 1,
        modifier = Modifier.background(color, RoundedCornerShape(50)).padding(horizontal = 8.dp, vertical = 4.dp))
}
