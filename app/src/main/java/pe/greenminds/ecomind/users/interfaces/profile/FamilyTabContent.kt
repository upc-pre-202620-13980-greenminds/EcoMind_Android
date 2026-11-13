package pe.greenminds.ecomind.users.interfaces.profile

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.theme.TextSecondary
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle
import pe.greenminds.ecomind.users.domain.model.FamilyMember
import pe.greenminds.ecomind.users.domain.model.FamilyOverview
import pe.greenminds.ecomind.users.domain.model.FamilyRelationship
import pe.greenminds.ecomind.users.domain.model.FamilyRole
import pe.greenminds.ecomind.users.domain.model.SocialRole
import pe.greenminds.ecomind.users.domain.model.UserProfile

@Composable
fun FamilyTabContent(
    profile: UserProfile,
    state: ProfileUiState,
    onRetry: () -> Unit,
    onNavigate: (Any) -> Unit
) {
    val family = state.family

    when {
        state.isLoadingFamily -> TabLoading()

        state.familyFailed -> TabError(onRetry = onRetry)

        family == null -> NoFamilyContent(
            isParent = profile.socialRole == SocialRole.PARENT
        )

        else -> FamilyContent(
            family = family,
            // Read again each time the profile opens, so it is already up to date after
            // equipping an avatar in Store, like the header
            currentUserAvatar = state.equippedCosmetics.avatar?.imageReference
        )
    }
}

// Families are created and managed in the EcoMind Parents app, so this tab only reads them
@Composable
private fun NoFamilyContent(isParent: Boolean) {
    EmptyTabMessage(
        title = stringResource(R.string.profile_family_empty_title),
        message = stringResource(
            if (isParent) {
                R.string.profile_family_empty_parent_message
            } else {
                R.string.profile_family_empty_student_message
            }
        ),
        titleSizeSp = 20
    )
    if (isParent) {
        Text(
            text = stringResource(R.string.profile_family_parents_app),
            style = interTextStyle(14, FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )
    }
}

@Composable
private fun FamilyContent(
    family: FamilyOverview,
    currentUserAvatar: String?
) {
    Text(
        text = family.name,
        style = interTextStyle(18, FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { heading() }
    )
    Text(
        text = family.commitment,
        style = interTextStyle(13),
        color = TextSecondary,
        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
    )

    Text(
        text = stringResource(R.string.profile_family_members, family.members.size),
        style = interTextStyle(16, FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .semantics { heading() }
    )

    family.members.forEach { member ->
        PersonRow(
            name = member.name,
            subtitle = memberSubtitle(member),
            trailingLabel = if (member.isCurrentUser) {
                stringResource(R.string.profile_row_you)
            } else {
                null
            },
            avatarImageReference = if (member.isCurrentUser) {
                currentUserAvatar
            } else {
                member.avatarImageReference
            },
            // The progress of a member (frame 22) is not built yet, so the rows do not open anything
            onClick = null
        )
    }
}

// "Student" or "Parent · family manager" for the user; "Daughter · 3/5 activities" for the rest
@Composable
private fun memberSubtitle(member: FamilyMember): String {
    if (member.isCurrentUser) {
        return if (member.familyRole == FamilyRole.PARENT) {
            stringResource(R.string.profile_family_manager)
        } else {
            stringResource(R.string.profile_role_student)
        }
    }

    val relationship = stringResource(
        when (member.relationship) {
            FamilyRelationship.MOTHER -> R.string.profile_relationship_mother
            FamilyRelationship.FATHER -> R.string.profile_relationship_father
            FamilyRelationship.DAUGHTER -> R.string.profile_relationship_daughter
            FamilyRelationship.SON -> R.string.profile_relationship_son
            // Without the provisional data, only the role of the web services is known
            null -> if (member.familyRole == FamilyRole.PARENT) {
                R.string.profile_role_parent
            } else {
                R.string.profile_role_student
            }
        }
    )

    val completed = member.activitiesCompleted
    val total = member.activitiesTotal
    return if (completed != null && total != null) {
        stringResource(R.string.profile_family_member_progress, relationship, completed, total)
    } else {
        relationship
    }
}
