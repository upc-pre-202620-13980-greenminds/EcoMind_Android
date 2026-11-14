package pe.greenminds.ecomind.users.interfaces.profile

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.theme.CardBorder
import pe.greenminds.ecomind.shared.interfaces.theme.TextPrimary
import pe.greenminds.ecomind.shared.interfaces.theme.TextSecondary
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle
import pe.greenminds.ecomind.gamification.interfaces.navigation.AchievementDetailRoute
import pe.greenminds.ecomind.users.domain.model.UserProfile
import pe.greenminds.ecomind.users.interfaces.navigation.AddCommitmentRoute
import pe.greenminds.ecomind.users.interfaces.navigation.EditCommitmentRoute
import pe.greenminds.ecomind.users.interfaces.navigation.MyMedalsRoute

// The profile only shows a few earned medals; the full list is in "View all"
private const val MEDALS_IN_PROFILE = 3

// Profile tab: the same for students and parents, their commitment and their medals
@Composable
fun ProfileTabContent(
    profile: UserProfile,
    state: ProfileUiState,
    onNavigate: (Any) -> Unit
) {
    CommitmentCard(
        commitment = profile.commitment,
        onAdd = { onNavigate(AddCommitmentRoute) },
        onEdit = { onNavigate(EditCommitmentRoute) }
    )

    Spacer(modifier = Modifier.height(16.dp))
    SectionHeaderRow(
        title = stringResource(R.string.profile_medals_title),
        action = stringResource(R.string.profile_view_all),
        onAction = { onNavigate(MyMedalsRoute) }
    )
    MedalsContent(
        state = state,
        onOpenMedal = { achievementId -> onNavigate(AchievementDetailRoute(achievementId)) }
    )
}

// If the medals fail to load, only the title and "View all" stay on screen
@Composable
private fun MedalsContent(
    state: ProfileUiState,
    onOpenMedal: (String) -> Unit
) {
    when {
        state.isLoadingAchievements -> TabLoading()

        state.achievementsFailed -> Unit

        state.myAchievements.isEmpty() -> Text(
            text = stringResource(R.string.profile_medals_empty),
            style = interTextStyle(13),
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        else -> state.myAchievements.take(MEDALS_IN_PROFILE).forEach { medal ->
            MedalRow(
                name = medal.name,
                onClick = { onOpenMedal(medal.achievementId) }
            )
        }
    }
}

@Composable
private fun CommitmentCard(
    commitment: String?,
    onAdd: () -> Unit,
    onEdit: () -> Unit
) {
    ProfileCard {
        if (commitment == null) {
            // Empty state: there is nothing to edit yet
            Text(
                text = stringResource(R.string.profile_commitment_title),
                style = interTextStyle(16, FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 12.dp)
                    .semantics { heading() }
            )
            Text(
                text = stringResource(R.string.profile_commitment_empty),
                style = interTextStyle(13),
                color = TextPrimary,
                modifier = Modifier.padding(top = 12.dp)
            )
            TextLink(
                text = stringResource(R.string.profile_commitment_add),
                onClick = onAdd
            )
        } else {
            SectionHeaderRow(
                title = stringResource(R.string.profile_commitment_title),
                action = stringResource(R.string.profile_edit),
                onAction = onEdit
            )
            Text(
                text = commitment,
                style = interTextStyle(13),
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }
}

// Card with the thin border of the design
@Composable
private fun ProfileCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp)
    ) {
        content()
    }
}

// Title on the left and a secondary action on the right
@Composable
fun SectionHeaderRow(
    title: String,
    action: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = interTextStyle(16, FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() }
        )
        TextLink(
            text = action,
            onClick = onAction,
            style = interTextStyle(12),
            color = TextSecondary
        )
    }
}
