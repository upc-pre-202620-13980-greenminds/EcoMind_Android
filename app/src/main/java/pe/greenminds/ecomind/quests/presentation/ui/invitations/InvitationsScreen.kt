package pe.greenminds.ecomind.quests.presentation.ui.invitations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.quests.domain.entity.CollabQuestInvitation
import pe.greenminds.ecomind.quests.presentation.states.InvitationsUiState
import pe.greenminds.ecomind.quests.presentation.viewmodel.InvitationsViewModel
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme

@Composable
fun InvitationsScreen(
    onBack: () -> Unit,
    viewModel: InvitationsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    InvitationsContent(
        state = state,
        onBack = onBack,
        onRetry = viewModel::loadInvitations,
        onRefresh = viewModel::refreshInvitations
    )
}

@Composable
internal fun InvitationsContent(
    state: InvitationsUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit = onRetry
) {
    QuestRefreshContainer(state.isRefreshing, onRefresh, enabled = !state.isLoading) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        PartyBackButton(onBack, Modifier.offset(x = (-12).dp))

        Text(
            text = stringResource(R.string.quest_invitations_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(24.dp))

        if (state.hasError) {
                Text(stringResource(R.string.quest_invitations_error))

                TextButton(onClick = onRetry) {
                    Text(stringResource(R.string.quest_try_again))
                }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!state.isLoading && !state.hasError && state.invitations.isEmpty()) {
                item {
                Text(stringResource(R.string.quest_invitations_empty))
                }
            }
                    items(
                        items = state.invitations,
                        key = { it.memberId }
                    ) { invitation ->
                        OutlinedCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    text = invitation.questTitle,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(stringResource(R.string.quest_invitation_message))
                            }
                        }
                    }
        }
    }
    }
}

@Preview(name = "Invitations · English", locale = "en", showBackground = true, widthDp = 360, heightDp = 720)
@Preview(name = "Invitaciones · Español", locale = "es-rPE", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun InvitationsPreview() {
    EcoMindTheme {
        Surface {
            InvitationsContent(
                state = InvitationsUiState(
                    invitations = listOf(
                        CollabQuestInvitation(
                            memberId = 1L,
                            sessionId = 10L,
                            questId = 18L,
                            questTitle = "Time to save up energy!",
                            organizerUserId = 2L,
                            invitedUserId = 3L
                        ),
                        CollabQuestInvitation(
                            memberId = 2L,
                            sessionId = 11L,
                            questId = 19L,
                            questTitle = "Every drop counts",
                            organizerUserId = 4L,
                            invitedUserId = 3L
                        )
                    ),
                    isLoading = false
                ),
                onBack = {},
                onRetry = {}
            )
        }
    }
}
