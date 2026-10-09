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
fun ProgressScreen(onBack: () -> Unit, onSignIn: () -> Unit, onHistory: () -> Unit,
    onMedals: () -> Unit, onGroup: (AchievementGroup) -> Unit, vm: ProgressViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.load() }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        AchievementHeader(stringResource(R.string.gamification_progress), onBack)
        LoadedContent(state, vm::load, onSignIn) { data ->
            if (data.simulated) DemoLabel()
            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                item {
                    Stat(stringResource(R.string.gamification_points), data.progress.ecopoints.toString())
                    Stat(stringResource(R.string.gamification_streak), data.progress.currentStreak.toString())
                    Stat(stringResource(R.string.gamification_longest_streak), data.progress.longestStreak.toString())
                    data.progress.lastActivityDate?.let { Stat(stringResource(R.string.gamification_last_activity), it) }
                    data.progress.lastProtectedDate?.let { Stat(stringResource(R.string.gamification_protected_day), it) }
                    Text(stringResource(R.string.gamification_streak_note), style = interTextStyle(12), color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp))
                    Button(onClick = onHistory, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.gamification_history)) }
                    OutlinedButton(onClick = onMedals, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.achievements_title)) }
                    data.familyEcopoints?.let { Stat(stringResource(R.string.gamification_family_points), it.toString()) }
                    DetailLabel(stringResource(R.string.gamification_collective))
                    if (data.groups.isEmpty()) Text(stringResource(R.string.gamification_no_groups), style = interTextStyle(14))
                }
                items(data.groups, key = { "${it.scope}:${it.id}" }) { group ->
                    TextButton(onClick = { onGroup(group) }, modifier = Modifier.fillMaxWidth()) { Text(groupName(group)) }
                }
                item { TextButton(onClick = vm::load) { Text(stringResource(R.string.gamification_refresh)) } }
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = interTextStyle(14), modifier = Modifier.weight(1f))
        Text(value, style = interTextStyle(16), color = MaterialTheme.colorScheme.primary)
    }
    HorizontalDivider()
}

@Composable
internal fun groupName(group: AchievementGroup): String = if (group.scope == "FAMILY") stringResource(R.string.gamification_family_medals) else group.name
