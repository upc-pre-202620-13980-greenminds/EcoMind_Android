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
fun GroupAchievementsScreen(group: AchievementGroup, onBack: () -> Unit, onSignIn: () -> Unit,
    vm: GroupAchievementsViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(group) { vm.load(group) }
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        AchievementHeader(groupName(group), onBack)
        LoadedContent(state, { vm.load(group) }, onSignIn) { collection ->
            if (collection.isSimulated) DemoLabel()
            if (collection.entries.isEmpty()) StateMessage(stringResource(R.string.gamification_no_collective))
            else LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                items(collection.entries, key = { it.achievement.id }) { entry ->
                    AchievementRow(entry, null)
                    Text(achievementDescription(entry.achievement), style = interTextStyle(14))
                    Text(requirement(entry.achievement), style = interTextStyle(12))
                    entry.award?.let { Text(it.awardedAt, style = interTextStyle(12)) }
                    HorizontalDivider(Modifier.padding(top = 12.dp))
                }
            }
        }
    }
}
