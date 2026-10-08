package pe.greenminds.ecomind.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.components.EcoMindTopBar
import pe.greenminds.ecomind.shared.interfaces.components.ImageTileButton
import pe.greenminds.ecomind.shared.interfaces.components.PillButton
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.SkyBlue
import pe.greenminds.ecomind.shared.interfaces.theme.SkyBlueDark
import pe.greenminds.ecomind.shared.interfaces.theme.SunYellow
import pe.greenminds.ecomind.shared.interfaces.theme.SunYellowDark

// Proportion of the tiles in the design (142 x 137)
private const val TILE_ASPECT_RATIO = 142f / 137f

@Composable
fun MainMenuScreen(
    onNavigateToPlaceholder: () -> Unit,
    viewModel: MainMenuViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    MainMenuContent(
        state = state,
        onNavigateToPlaceholder = onNavigateToPlaceholder
    )
}

// Every destination of this screen is the same placeholder for now
@Composable
private fun MainMenuContent(
    state: MainMenuUiState,
    onNavigateToPlaceholder: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
    ) {
        EcoMindTopBar(
            gemBalance = state.gemBalance,
            ecopoints = state.ecopoints,
            onNotificationsClick = onNavigateToPlaceholder,
            onSettingsClick = onNavigateToPlaceholder
        )

        // The middle takes the free space and scrolls on small screens or large fonts
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
        ) {
            if (state.errorMessage != null) {
                Text(
                    text = stringResource(state.errorMessage),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .semantics { liveRegion = LiveRegionMode.Polite }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
            PillButton(
                text = stringResource(R.string.main_menu_energy),
                onClick = onNavigateToPlaceholder,
                faceColor = SunYellow,
                baseColor = SunYellowDark,
                iconRes = R.drawable.ic_lightbulb
            )

            Spacer(modifier = Modifier.height(40.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(21.dp)) {
                ImageTileButton(
                    defaultRes = R.drawable.img_tile_quests_default,
                    pressedRes = R.drawable.img_tile_quests_pressed,
                    contentDescription = stringResource(R.string.main_menu_quests),
                    onClick = onNavigateToPlaceholder,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(TILE_ASPECT_RATIO)
                )
                ImageTileButton(
                    defaultRes = R.drawable.img_tile_minigames_default,
                    pressedRes = R.drawable.img_tile_minigames_pressed,
                    contentDescription = stringResource(R.string.main_menu_minigames),
                    onClick = onNavigateToPlaceholder,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(TILE_ASPECT_RATIO)
                )
            }
            Spacer(modifier = Modifier.height(19.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(21.dp)) {
                ImageTileButton(
                    defaultRes = R.drawable.img_tile_collaborative_default,
                    pressedRes = R.drawable.img_tile_collaborative_pressed,
                    contentDescription = stringResource(R.string.main_menu_collaborative),
                    onClick = onNavigateToPlaceholder,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(TILE_ASPECT_RATIO)
                )
                ImageTileButton(
                    defaultRes = R.drawable.img_tile_videos_default,
                    pressedRes = R.drawable.img_tile_videos_pressed,
                    contentDescription = stringResource(R.string.main_menu_videos),
                    onClick = onNavigateToPlaceholder,
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(TILE_ASPECT_RATIO)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            IconButton(onClick = onNavigateToPlaceholder) {
                Image(
                    painter = painterResource(R.drawable.ic_more_horiz),
                    contentDescription = stringResource(R.string.main_menu_more),
                    modifier = Modifier.size(33.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            PillButton(
                text = stringResource(R.string.main_menu_learn_more),
                onClick = onNavigateToPlaceholder,
                faceColor = SkyBlue,
                baseColor = SkyBlueDark,
                iconRes = R.drawable.ic_lightbulb
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        MainNavigationBar(
            selectedItem = NavigationItem.MAIN_MENU,
            onItemClick = { item ->
                // The main menu is already on screen; the others are not built yet
                if (item != NavigationItem.MAIN_MENU) {
                    onNavigateToPlaceholder()
                }
            }
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun MainMenuContentPreview() {
    EcoMindTheme {
        MainMenuContent(
            state = MainMenuUiState(gemBalance = 360, ecopoints = 16, isLoading = false),
            onNavigateToPlaceholder = {}
        )
    }
}
