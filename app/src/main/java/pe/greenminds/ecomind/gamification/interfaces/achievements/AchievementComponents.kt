package pe.greenminds.ecomind.gamification.interfaces.achievements

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.gamification.domain.model.Achievement
import pe.greenminds.ecomind.gamification.domain.model.AchievementEntry
import pe.greenminds.ecomind.shared.interfaces.components.PillButton
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreen
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreenDark
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle

@Composable
internal fun AchievementHeader(title: String, onBack: () -> Unit) {
    Box(Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
            Icon(painterResource(R.drawable.ic_chevron_back), stringResource(R.string.achievements_back),
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        }
        Text(title, style = interTextStyle(20, FontWeight.Bold), color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.align(Alignment.Center).padding(horizontal = 32.dp).semantics { heading() })
    }
}

@Composable
internal fun ColumnScope.LoadingContent() {
    val loading = stringResource(R.string.loading)
    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(Modifier.semantics { contentDescription = loading })
    }
}

@Composable
internal fun ColumnScope.RecoveryContent(sessionRequired: Boolean, onRetry: () -> Unit, onSignIn: () -> Unit) {
    StateMessage(stringResource(if (sessionRequired) R.string.achievements_session_required else R.string.achievements_error))
    PillButton(
        text = stringResource(if (sessionRequired) R.string.achievements_sign_in else R.string.ranking_try_again),
        onClick = if (sessionRequired) onSignIn else onRetry,
        faceColor = EcoGreen, baseColor = EcoGreenDark, height = 48.dp,
        textStyle = interTextStyle(16, FontWeight.Bold), modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
internal fun ColumnScope.StateMessage(message: String) {
    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(message, style = interTextStyle(14), color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center,
            modifier = Modifier.padding(24.dp).semantics { liveRegion = LiveRegionMode.Polite })
    }
}

@Composable
internal fun DemoLabel() {
    Text(stringResource(R.string.achievements_demo), color = MaterialTheme.colorScheme.onSurfaceVariant, style = interTextStyle(12),
        modifier = Modifier.padding(bottom = 12.dp))
}

@Composable
internal fun AchievementRow(entry: AchievementEntry, onClick: (() -> Unit)?) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Image(painterResource(if (entry.achievement.code == "DEMO_SECTION") R.drawable.ic_achievement_section else R.drawable.ic_achievement_activity),
            contentDescription = null, modifier = Modifier.size(44.dp))
        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(achievementName(entry.achievement), style = interTextStyle(14, FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            Text(stringResource(if (entry.award != null) R.string.achievement_awarded else R.string.achievement_not_earned),
                style = interTextStyle(12), color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
        }
        Text(stringResource(if (entry.award != null) R.string.achievements_earned else R.string.achievement_locked),
            style = interTextStyle(13, FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
internal fun DetailLabel(text: String) {
    Text(text, style = interTextStyle(16, FontWeight.Bold), color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 24.dp, bottom = 8.dp).semantics { heading() })
}

@Composable
private fun achievementName(achievement: Achievement): String = when (achievement.code) {
    "DEMO_SECTION" -> stringResource(R.string.achievement_demo_section)
    "DEMO_ACTIVITY" -> stringResource(R.string.achievement_demo_activity)
    "DEMO_STREAK" -> stringResource(R.string.achievement_demo_streak)
    else -> achievement.name
}

@Composable
internal fun achievementDescription(achievement: Achievement): String = when (achievement.code) {
    "DEMO_SECTION" -> stringResource(R.string.achievement_demo_section_description)
    "DEMO_ACTIVITY" -> stringResource(R.string.achievement_demo_activity_description)
    "DEMO_STREAK" -> stringResource(R.string.achievement_demo_streak_description)
    else -> achievement.description
}

@Composable
internal fun requirement(achievement: Achievement): String = when (achievement.metric) {
    "ECOPOINTS", "EXPERIENCE" -> stringResource(R.string.achievement_ecopoints_requirement, achievement.target)
    "LONGEST_STREAK" -> stringResource(R.string.achievement_streak_requirement, achievement.target)
    "COMPLETED_COMMUNITY_GOALS" -> stringResource(R.string.achievement_goals_requirement, achievement.target)
    "COMPLETED_FAMILY_PLANS" -> stringResource(R.string.achievement_plans_requirement, achievement.target)
    else -> achievement.description
}
