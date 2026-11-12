package pe.greenminds.ecomind.quests.presentation.minigames.collectwaste

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.quests.presentation.ui.execution.QuestButton
import pe.greenminds.ecomind.quests.presentation.ui.execution.QuestCompletion
import pe.greenminds.ecomind.quests.presentation.ui.invitations.PartyBackButton
import pe.greenminds.ecomind.quests.presentation.ui.theme.LightQuestColorScheme

private val wasteIcons = listOf("🧴", "🥫", "📰")
private val wasteLabels = listOf(R.string.minigame_bottle, R.string.minigame_can, R.string.minigame_paper)

@Composable
internal fun CollectWasteScreen(onBack: () -> Unit, viewModel: CollectWasteViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    BackHandler(onBack = viewModel::back)
    LaunchedEffect(state) {
        if (state == CollectWasteState.Closed) onBack()
    }

    val result = state as? CollectWasteState.Result
    if (result?.successful == true) {
        CollectWasteCompletion(result, viewModel::replay, viewModel::retryReward, viewModel::back)
        return
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PartyBackButton(viewModel::back)
        if (state is CollectWasteState.Playing || state is CollectWasteState.Result) {
            Text(
                stringResource(R.string.minigame_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
        when (val current = state) {
            CollectWasteState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally))
            CollectWasteState.Closed -> Unit
            CollectWasteState.Unsupported -> Text(stringResource(R.string.minigame_unsupported_android))
            is CollectWasteState.Playing -> {
                Text(stringResource(R.string.minigame_instructions, current.minScore))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.minigame_time, current.seconds), fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.minigame_score, current.score), fontWeight = FontWeight.Bold)
                }
                WasteBoard(current.target, viewModel::collect, Modifier.align(Alignment.CenterHorizontally))
            }
            is CollectWasteState.Result -> {
                Text(stringResource(R.string.minigame_result), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.minigame_score, current.score), fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.minigame_unsuccessful))
                QuestButton(stringResource(R.string.minigame_replay), viewModel::replay, true, false)
            }
            is CollectWasteState.Error -> {
                val message = when (current.action) {
                    CollectWasteAction.START -> if (current.hasOtherActiveAttempt) R.string.minigame_active_attempt
                        else R.string.minigame_start_error
                    CollectWasteAction.FINISH -> R.string.minigame_finish_error
                    CollectWasteAction.CANCEL -> R.string.minigame_cancel_error
                }
                Text(stringResource(message), color = MaterialTheme.colorScheme.error)
                QuestButton(stringResource(R.string.quest_try_again), viewModel::retry, true, false)
            }
        }
    }
}

@Composable
private fun WasteBoard(target: WasteTarget, onCollect: (WasteTarget) -> Unit, modifier: Modifier = Modifier) {
    val colors = LightQuestColorScheme.minigame
    val cellShape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier.widthIn(max = 330.dp).fillMaxWidth()
            .background(colors.base, RoundedCornerShape(24.dp)).padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(3) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { column ->
                    val active = target.cell == row * 3 + column
                    val label = if (active) stringResource(wasteLabels[target.kind]) else ""
                    val cell = Modifier.weight(1f).aspectRatio(1f).clip(cellShape)
                        .background(if (active) colors.top else colors.face)
                    Box(
                        modifier = if (active) cell.clickable(role = Role.Button, onClickLabel = label) {
                            onCollect(target)
                        } else cell,
                        contentAlignment = Alignment.Center
                    ) {
                        if (active) {
                            Text(wasteIcons[target.kind], Modifier.clearAndSetSemantics { contentDescription = label },
                                fontSize = 34.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectWasteCompletion(
    result: CollectWasteState.Result,
    onReplay: () -> Unit,
    onRetryReward: () -> Unit,
    onBack: () -> Unit
) {
    val reward = result.reward
    val rewardText = reward?.let { stringResource(R.string.minigame_confirmed_reward, it.ecopoints) }
    Column(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)
            .safeDrawingPadding().padding(horizontal = 28.dp)
    ) {
        PartyBackButton(onBack)
        QuestCompletion(
            rewardText = rewardText,
            gems = reward?.gems ?: 0,
            actionLabel = stringResource(R.string.minigame_replay),
            onAction = onReplay
        ) {
            Text(stringResource(R.string.minigame_score, result.score), Modifier.align(Alignment.CenterHorizontally))
            if (reward == null) {
                Text(stringResource(R.string.minigame_reward_pending), textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth())
                TextButton(onClick = onRetryReward, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text(stringResource(R.string.minigame_check_reward))
                }
            } else if (reward.ecopoints == 0L) {
                Text(stringResource(R.string.minigame_zero_reward), textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth())
            }
        }
    }
}
