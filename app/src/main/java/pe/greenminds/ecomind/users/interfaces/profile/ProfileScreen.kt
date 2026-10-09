package pe.greenminds.ecomind.users.interfaces.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.monetization.domain.model.CosmeticType
import pe.greenminds.ecomind.monetization.domain.model.EquippedCosmetics
import pe.greenminds.ecomind.monetization.interfaces.store.cosmeticImageResource
import pe.greenminds.ecomind.shared.interfaces.components.InitialsAvatar
import pe.greenminds.ecomind.shared.interfaces.components.PillButton
import pe.greenminds.ecomind.shared.interfaces.components.SegmentedTabs
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreen
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreenDark
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.TextPrimary
import pe.greenminds.ecomind.shared.interfaces.theme.TextSecondary
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle
import pe.greenminds.ecomind.users.domain.model.SocialRole
import pe.greenminds.ecomind.users.domain.model.UserProfile
import pe.greenminds.ecomind.users.interfaces.navigation.EditProfileRoute
import pe.greenminds.ecomind.users.interfaces.navigation.FavoritesRoute
import pe.greenminds.ecomind.users.interfaces.navigation.ShareProfileRoute

// The top bar and the bottom bar are drawn by MainShell; this is only the center of the screen.
// onNavigate receives one of the hook routes declared in UsersNavGraph.
@Composable
fun ProfileScreen(
    onNavigate: (Any) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    // The Profile ViewModel can remain alive while the user equips an item in Store.
    // Refresh the overlay whenever this destination enters composition again.
    LaunchedEffect(Unit) {
        viewModel.loadEquippedCosmetics()
    }

    ProfileContent(
        state = state,
        onTabSelected = viewModel::onTabSelected,
        onRetryProfile = viewModel::loadProfile,
        onRetryFriends = viewModel::retryFriends,
        onRetryFamily = viewModel::retryFamily,
        onNavigate = onNavigate
    )
}

@Composable
private fun ProfileContent(
    state: ProfileUiState,
    onTabSelected: (ProfileTab) -> Unit,
    onRetryProfile: () -> Unit,
    onRetryFriends: () -> Unit,
    onRetryFamily: () -> Unit,
    onNavigate: (Any) -> Unit,
    modifier: Modifier = Modifier
) {
    val profile = state.profile

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            state.isLoadingProfile -> TabLoading(modifier = Modifier.align(Alignment.Center))

            state.profileFailed || profile == null -> ProfileErrorContent(onRetry = onRetryProfile)

            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                ProfileHeader(
                    profile = profile,
                    equippedCosmetics = state.equippedCosmetics,
                    onNavigate = onNavigate
                )

                Spacer(modifier = Modifier.height(12.dp))
                SegmentedTabs(
                    options = listOf(
                        stringResource(R.string.profile_tab_profile),
                        stringResource(R.string.profile_tab_friends),
                        stringResource(R.string.profile_tab_family)
                    ),
                    selectedIndex = state.selectedTab.ordinal,
                    onOptionSelected = { index -> onTabSelected(ProfileTab.entries[index]) }
                )
                Spacer(modifier = Modifier.height(16.dp))

                when (state.selectedTab) {
                    ProfileTab.PROFILE -> ProfileTabContent(
                        profile = profile,
                        state = state,
                        onNavigate = onNavigate
                    )

                    ProfileTab.FRIENDS -> FriendsTabContent(
                        state = state,
                        onRetry = onRetryFriends,
                        onNavigate = onNavigate
                    )

                    ProfileTab.FAMILY -> FamilyTabContent(
                        profile = profile,
                        state = state,
                        onRetry = onRetryFamily,
                        onNavigate = onNavigate
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// Part shared by the three tabs: title, avatar, name, role, statistics and actions
@Composable
private fun ProfileHeader(
    profile: UserProfile,
    equippedCosmetics: EquippedCosmetics,
    onNavigate: (Any) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.profile_title),
                style = interTextStyle(20, FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.Center)
                    .semantics { heading() }
            )
            TextLink(
                text = stringResource(R.string.profile_edit),
                onClick = { onNavigate(EditProfileRoute) },
                style = interTextStyle(13),
                color = TextSecondary,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }

        ProfileAvatar(
            name = profile.name,
            equippedCosmetics = equippedCosmetics
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = profile.name,
            style = interTextStyle(24, FontWeight.Bold),
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(
                if (profile.socialRole == SocialRole.PARENT) {
                    R.string.profile_role_parent
                } else {
                    R.string.profile_role_student
                }
            ),
            style = interTextStyle(12),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            ProfileStat(
                value = profile.streak,
                label = stringResource(R.string.profile_stat_streak),
                modifier = Modifier.weight(1f)
            )
            ProfileStat(
                value = profile.ecopoints,
                label = stringResource(R.string.profile_stat_ecopoints),
                modifier = Modifier.weight(1f)
            )
            ProfileStat(
                value = profile.gemBalance,
                label = stringResource(R.string.profile_stat_gems),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        ) {
            // The design draws 39dp; 48dp keeps the minimum touch target
            PillButton(
                text = stringResource(R.string.profile_share),
                onClick = { onNavigate(ShareProfileRoute) },
                faceColor = EcoGreen,
                baseColor = EcoGreenDark,
                height = 48.dp,
                textStyle = interTextStyle(16, FontWeight.Bold),
                modifier = Modifier.weight(1f)
            )
            PillButton(
                text = stringResource(R.string.profile_favorites),
                onClick = { onNavigate(FavoritesRoute) },
                faceColor = EcoGreen,
                baseColor = EcoGreenDark,
                height = 48.dp,
                textStyle = interTextStyle(16, FontWeight.Bold),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ProfileAvatar(
    name: String,
    equippedCosmetics: EquippedCosmetics
) {
    val avatar = equippedCosmetics.avatar
    val overlay = equippedCosmetics.overlay

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(100.dp)
    ) {
        if (avatar == null) {
            InitialsAvatar(name = name, size = 76.dp)
        } else {
            Image(
                painter = painterResource(cosmeticImageResource(avatar.imageReference)),
                contentDescription = avatar.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(76.dp)
            )
        }

        overlay?.let { cosmetic ->
            val isHeadItem = cosmetic.type == CosmeticType.HEAD ||
                cosmetic.imageReference == "cosmetic_bun"
            Image(
                painter = painterResource(cosmeticImageResource(cosmetic.imageReference)),
                contentDescription = cosmetic.name,
                contentScale = ContentScale.Fit,
                modifier = if (isHeadItem) {
                    Modifier
                        .align(Alignment.TopCenter)
                        .offset { IntOffset(0, -4.dp.roundToPx()) }
                        .size(44.dp)
                } else {
                    Modifier
                        .align(Alignment.BottomEnd)
                        .size(28.dp)
                }
            )
        }
    }
}

@Composable
private fun ProfileStat(
    value: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        // The screen reader says "2, Day streak" as one item
        modifier = modifier.clearAndSetSemantics { contentDescription = "$value, $label" }
    ) {
        Text(
            text = value.toString(),
            style = interTextStyle(24, FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = interTextStyle(12),
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

private val previewProfile = UserProfile(
    id = 1L,
    name = "EcoMind",
    socialRole = SocialRole.STUDENT,
    streak = 2,
    ecopoints = 16,
    gemBalance = 360
)

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun ProfileContentPreview() {
    EcoMindTheme {
        ProfileContent(
            state = ProfileUiState(
                isLoadingProfile = false,
                profile = previewProfile,
                isLoadingFriends = false,
                isLoadingFamily = false
            ),
            onTabSelected = {},
            onRetryProfile = {},
            onRetryFriends = {},
            onRetryFamily = {},
            onNavigate = {}
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun ProfileContentErrorPreview() {
    EcoMindTheme {
        ProfileContent(
            state = ProfileUiState(isLoadingProfile = false, profileFailed = true),
            onTabSelected = {},
            onRetryProfile = {},
            onRetryFriends = {},
            onRetryFamily = {},
            onNavigate = {}
        )
    }
}
