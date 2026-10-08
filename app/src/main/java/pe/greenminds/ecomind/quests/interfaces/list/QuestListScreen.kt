package pe.greenminds.ecomind.quests.interfaces.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.TextSecondary
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle

// The top bar and the bottom bar are drawn by MainShell; this is only the center of the screen
@Composable
fun QuestListScreen(
    onOpenQuest: (Long) -> Unit,
    onOpenFilters: () -> Unit,
    viewModel: QuestListViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    QuestListContent(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onSearch = viewModel::searchNow,
        onOpenQuest = onOpenQuest,
        onOpenFilters = onOpenFilters
    )
}

@Composable
private fun QuestListContent(
    state: QuestListUiState,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onOpenQuest: (Long) -> Unit,
    onOpenFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        QuestSearchBar(
            query = state.query,
            requestFocus = state.focusSearch,
            onQueryChange = onQueryChange,
            onSearch = onSearch,
            onOpenFilters = onOpenFilters,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.hasError -> QuestListMessage(
                    message = stringResource(R.string.quest_list_error),
                    actionLabel = stringResource(R.string.quest_try_again),
                    onAction = onSearch
                )

                // The previous results stay on screen while a new search runs
                state.isLoading && state.quests.isEmpty() -> {
                    val loadingDescription = stringResource(R.string.loading)
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .semantics { contentDescription = loadingDescription }
                    )
                }

                state.quests.isEmpty() -> QuestListMessage(
                    message = stringResource(R.string.quest_list_empty)
                )

                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // The id as key keeps each card stable when the results change
                    items(state.quests, key = { quest -> quest.id }) { quest ->
                        QuestCard(
                            quest = quest,
                            onOpen = { onOpenQuest(quest.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestSearchBar(
    query: String,
    requestFocus: Boolean,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onOpenFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    val searchLabel = stringResource(R.string.quest_search_placeholder)

    // Opened from "Search": the keyboard appears without an extra tap
    LaunchedEffect(requestFocus) {
        if (requestFocus) {
            focusRequester.requestFocus()
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(percent = 50))
    ) {
        IconButton(onClick = onOpenFilters) {
            Icon(
                painter = painterResource(R.drawable.ic_menu),
                contentDescription = stringResource(R.string.quest_open_filters)
            )
        }
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    focusManager.clearFocus()
                    onSearch()
                }
            ),
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
                .semantics { contentDescription = searchLabel },
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(
                            text = searchLabel,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextSecondary
                        )
                    }
                    innerTextField()
                }
            }
        )
        IconButton(
            onClick = {
                focusManager.clearFocus()
                onSearch()
            }
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_search),
                contentDescription = stringResource(R.string.quest_search_action)
            )
        }
    }
}

// The design has no empty or error state for the list, so these are kept minimal
@Composable
private fun QuestListMessage(
    message: String,
    actionLabel: String? = null,
    onAction: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, start = 24.dp, end = 24.dp)
    ) {
        Text(
            text = message,
            style = interTextStyle(13),
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
        )
        if (actionLabel != null) {
            TextButton(onClick = onAction) {
                Text(text = actionLabel)
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun QuestListContentEmptyPreview() {
    EcoMindTheme {
        QuestListContent(
            state = QuestListUiState(isLoading = false),
            onQueryChange = {},
            onSearch = {},
            onOpenQuest = {},
            onOpenFilters = {}
        )
    }
}
