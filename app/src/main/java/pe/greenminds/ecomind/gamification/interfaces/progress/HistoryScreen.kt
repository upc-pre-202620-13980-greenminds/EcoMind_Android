package pe.greenminds.ecomind.gamification.interfaces.progress

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.gamification.domain.model.*
import pe.greenminds.ecomind.gamification.interfaces.achievements.*
import pe.greenminds.ecomind.shared.interfaces.components.SegmentedTabs
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle

@Composable
fun HistoryScreen(onBack: () -> Unit, onSignIn: () -> Unit, vm: HistoryViewModel = hiltViewModel()) {
    var selected by rememberSaveable { mutableIntStateOf(RankingPeriod.ALL_TIME.ordinal) }
    val period = RankingPeriod.entries[selected]
    val state by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(period) { vm.load(period) }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        AchievementHeader(stringResource(R.string.gamification_history), onBack)
        if (!pe.greenminds.ecomind.BuildConfig.REMOTE_BACKEND) DemoLabel()
        SegmentedTabs(listOf(stringResource(R.string.ranking_period_daily), stringResource(R.string.ranking_period_weekly),
            stringResource(R.string.ranking_period_monthly), stringResource(R.string.ranking_period_all_time)), selected, { selected = it })
        LoadedContent(state, { vm.load(period) }, onSignIn) { rewards ->
            if (rewards.isEmpty()) StateMessage(stringResource(R.string.gamification_no_rewards))
            else LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                items(rewards, key = { it.id }) { reward ->
                    Column(Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                        Text(rewardSource(reward.source), color = MaterialTheme.colorScheme.primary, style = interTextStyle(16))
                        Text(stringResource(R.string.gamification_reward_amount, reward.ecopoints, reward.gems), style = interTextStyle(14))
                        Text(reward.occurredAt, style = interTextStyle(12))
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun rewardSource(source: String): String = stringResource(when (source) {
    "QUEST" -> R.string.gamification_source_quest
    "MINIGAME" -> R.string.gamification_source_minigame
    "COLLABORATIVE_QUEST" -> R.string.gamification_source_collaborative
    "FAMILY_PLAN" -> R.string.gamification_source_family
    "COMMUNITY_GOAL" -> R.string.gamification_source_goal
    "COMMUNITY_EVENT" -> R.string.gamification_source_event
    else -> R.string.gamification_source_other
})
