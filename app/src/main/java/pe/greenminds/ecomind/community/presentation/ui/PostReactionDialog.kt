package pe.greenminds.ecomind.community.presentation.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.community.domain.valueobjects.PostReactionType
import pe.greenminds.ecomind.community.presentation.states.CommunityNewsUiState

@Composable
fun PostReactionsDialog(
    state: CommunityNewsUiState,
    onSelect: (PostReactionType) -> Unit,
    onRemove: () -> Unit,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    val error = state.reactionError
        ?: state.reactionErrorRes?.let { stringResource(it) }

    Dialog(
        onDismissRequest = {
            if (!state.isSavingReaction) onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.9f),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFE4F5DA),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(
                        if (state.showPicker) {
                            R.string.news_choose_reaction
                        } else {
                            R.string.news_reactions_title
                        }
                    ),
                    style = MaterialTheme.typography.titleLarge
                )

                when {
                    state.isLoadingReactions -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }

                    error != null -> {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error
                        )
                        TextButton(onClick = onRetry) {
                            Text(stringResource(R.string.news_retry))
                        }
                    }

                    else -> {
                        state.reactions?.let { summary ->
                            Text(
                                stringResource(
                                    R.string.news_reactions_count,
                                    summary.total
                                )
                            )

                            PostReactionType.entries.forEach { type ->
                                val selected = summary.ownReaction == type
                                val background = if (selected && state.showPicker) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    Color.White
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            background,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable(
                                            enabled = state.showPicker &&
                                                    !state.isSavingReaction,
                                            onClick = { onSelect(type) }
                                        )
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Image(
                                        painter = painterResource(type.imageRes()),
                                        contentDescription = null,
                                        modifier = Modifier.size(30.dp)
                                    )

                                    Text(
                                        text = stringResource(type.labelRes()),
                                        modifier = Modifier.weight(1f)
                                    )

                                    if (selected && state.showPicker) {
                                        Text(
                                            text = stringResource(R.string.news_your_reaction),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }

                                    Text(
                                        text = (summary.counts[type] ?: 0).toString()
                                    )
                                }
                            }

                            if (state.showPicker && summary.ownReaction != null) {
                                TextButton(
                                    onClick = onRemove,
                                    enabled = !state.isSavingReaction
                                ) {
                                    Text(stringResource(R.string.news_remove_reaction))
                                }
                            }
                        }
                    }
                }

                if (state.isSavingReaction) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }

                TextButton(
                    onClick = onDismiss,
                    enabled = !state.isSavingReaction,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(stringResource(R.string.news_close))
                }
            }
        }
    }
}