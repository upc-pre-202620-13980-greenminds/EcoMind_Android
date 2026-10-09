package pe.greenminds.ecomind.quests.presentation.ui.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.quests.presentation.states.ProgressUiState
import pe.greenminds.ecomind.quests.presentation.viewmodel.ProgressViewModel
import pe.greenminds.ecomind.shared.interfaces.components.ComingSoonDialog
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme

// The top bar and the bottom bar are drawn by MainShell; this is only the center of the screen
@Composable
fun ProgressScreen(viewModel: ProgressViewModel = hiltViewModel()) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    ProgressContent(state = state, onRetry = viewModel::loadProgress)
}

@Composable
internal fun ProgressContent(
    state: ProgressUiState,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {}
) {
    // The two actions of this screen are drawn but not built yet
    var showComingSoon by rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (state.isLoading) {
            val loadingDescription = stringResource(R.string.loading)
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.Center)
                    .semantics { contentDescription = loadingDescription }
            )
        } else if (state.hasError) {
            Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.quest_list_error))
                TextButton(onClick = onRetry) { Text(stringResource(R.string.quest_try_again)) }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 28.dp, vertical = 24.dp)
            ) {
                ProgressSectionCard(
                    label = stringResource(R.string.progress_in_progress),
                    entries = state.inProgress,
                    actionLabel = stringResource(R.string.progress_see_more),
                    onAction = { showComingSoon = true }
                )
                ProgressSectionCard(
                    label = stringResource(R.string.progress_family_quest),
                    entries = state.familyQuests,
                    actionLabel = stringResource(R.string.progress_see_family_quest),
                    onAction = { showComingSoon = true }
                )
                ProgressSectionCard(
                    label = stringResource(R.string.progress_finished),
                    entries = state.finished
                )
            }
        }
    }

    if (showComingSoon) {
        ComingSoonDialog(onDismiss = { showComingSoon = false })
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ProgressContentPreview() {
    val sample = stringResource(R.string.app_name)

    EcoMindTheme {
        ProgressContent(
            state = ProgressUiState(
                isLoading = false,
                inProgress = listOf(previewProgressEntry(1L, sample, 70), previewProgressEntry(2L, sample, 30)),
                familyQuests = listOf(previewProgressEntry(3L, sample, 70), previewProgressEntry(4L, sample, 30)),
                finished = listOf(previewProgressEntry(5L, sample, 100))
            )
        )
    }
}
