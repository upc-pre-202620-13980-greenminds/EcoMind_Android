package pe.greenminds.ecomind.users.interfaces.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import pe.greenminds.ecomind.shared.interfaces.theme.TextSecondary
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle
import pe.greenminds.ecomind.users.domain.model.FamilyMember
import pe.greenminds.ecomind.users.domain.model.FamilyOverview
import pe.greenminds.ecomind.users.domain.model.FamilyRelationship
import pe.greenminds.ecomind.users.domain.model.FamilyRole
import pe.greenminds.ecomind.users.domain.model.SocialRole
import pe.greenminds.ecomind.users.domain.model.UserProfile
import pe.greenminds.ecomind.users.interfaces.navigation.CreateFamilyRoute
import pe.greenminds.ecomind.users.interfaces.navigation.FamilyActivityProgressRoute
import pe.greenminds.ecomind.users.interfaces.navigation.FamilyWeeklyReportRoute
import pe.greenminds.ecomind.users.interfaces.navigation.ManageFamilyRoute

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
            canCreateFamily = profile.socialRole == SocialRole.PARENT,
            onCreateFamily = { onNavigate(CreateFamilyRoute) }
        )

        else -> FamilyContent(family = family, onNavigate = onNavigate)
    }
}

// Only a parent can create a family, so a student just reads the explanation
@Composable
private fun NoFamilyContent(
    canCreateFamily: Boolean,
    onCreateFamily: () -> Unit
) {
    EmptyTabMessage(
        title = stringResource(R.string.profile_family_empty_title),
        message = stringResource(
            if (canCreateFamily) {
                R.string.profile_family_empty_parent_message
            } else {
                R.string.profile_family_empty_student_message
            }
        ),
        titleSizeSp = 20
    )
    if (canCreateFamily) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            TextLink(
                text = stringResource(R.string.profile_family_create),
                onClick = onCreateFamily,
                style = interTextStyle(14, FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun FamilyContent(
    family: FamilyOverview,
    onNavigate: (Any) -> Unit
) {
    // "Manage" and "Weekly report" are only for the parents of the family
    val isManager = family.isManagedByCurrentUser

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = family.name,
            style = interTextStyle(18, FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() }
        )
        if (isManager) {
            TextLink(
                text = stringResource(R.string.profile_family_manage),
                onClick = { onNavigate(ManageFamilyRoute) }
            )
        }
    }
    Text(
        text = family.commitment,
        style = interTextStyle(13),
        color = TextSecondary,
        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.profile_family_members, family.members.size),
            style = interTextStyle(16, FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .weight(1f)
                .semantics { heading() }
        )
        if (isManager) {
            TextLink(
                text = stringResource(R.string.profile_family_weekly_report),
                onClick = { onNavigate(FamilyWeeklyReportRoute) }
            )
        }
    }

    family.members.forEach { member ->
        PersonRow(
            name = member.name,
            subtitle = memberSubtitle(member),
            trailingLabel = if (member.isCurrentUser) {
                stringResource(R.string.profile_row_you)
            } else {
                null
            },
            // In the design only a parent opens the progress of another member
            onClick = if (isManager && !member.isCurrentUser) {
                { onNavigate(FamilyActivityProgressRoute(member.userId)) }
            } else {
                null
            }
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
