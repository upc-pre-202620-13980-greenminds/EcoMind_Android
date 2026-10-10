package pe.greenminds.ecomind.gamification.interfaces.achievements

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle

@Composable
fun AchievementDetailScreen(
    achievementId: String,
    onBack: () -> Unit,
    onSignIn: () -> Unit,
    viewModel: AchievementDetailViewModel = hiltViewModel(),
    onShare: (String) -> Unit = {}
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    LaunchedEffect(achievementId) {
        viewModel.loadAchievementById(achievementId)
    }
    AchievementDetailContent(uiState, onBack,
        onRetry = { viewModel.loadAchievementById(achievementId) }, onSignIn = onSignIn, onShare = onShare)
}

@Composable
internal fun AchievementDetailContent(
    state: AchievementDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onSignIn: () -> Unit,
    onShare: (String) -> Unit = {}
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        AchievementHeader(stringResource(R.string.achievement_detail_title), onBack)
        when (state) {
            AchievementDetailUiState.Loading -> LoadingContent()
            AchievementDetailUiState.Error -> RecoveryContent(false, onRetry, onSignIn)
            AchievementDetailUiState.SessionRequired -> RecoveryContent(true, onRetry, onSignIn)
            AchievementDetailUiState.NotFound -> StateMessage(stringResource(R.string.achievement_not_found))
            is AchievementDetailUiState.Success -> {
                if (state.detail.isSimulated) DemoLabel()
                val entry = state.detail.entry
                LazyColumn(contentPadding = PaddingValues(vertical = 16.dp)) {
                    item {
                        AchievementRow(entry, onClick = null)
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        DetailLabel(stringResource(R.string.achievement_about))
                        Text(achievementDescription(entry.achievement), style = interTextStyle(14), color = MaterialTheme.colorScheme.onSurface)
                        DetailLabel(stringResource(R.string.achievement_requirement))
                        Text(requirement(entry.achievement), style = interTextStyle(14), color = MaterialTheme.colorScheme.onSurface)
                        if (entry.award != null) {
                            DetailLabel(stringResource(R.string.achievement_awarded_at))
                            // Keep the backend's original instant; never invent a completion date.
                            Text(entry.award.awardedAt, style = interTextStyle(14), color = MaterialTheme.colorScheme.onSurface)
                            if (!state.detail.isSimulated) androidx.compose.material3.OutlinedButton(onClick = { onShare(entry.award.id) }) {
                                Text(stringResource(R.string.gamification_share))
                            }
                        }
                        if (!entry.achievement.active) {
                            DetailLabel(stringResource(R.string.achievement_archived))
                        }
                        Text(stringResource(R.string.achievement_impact_note),
                            style = interTextStyle(12), color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 24.dp))
                    }
                }
            }
        }
    }
}
