package pe.greenminds.ecomind.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.components.ImageTileButton
import pe.greenminds.ecomind.shared.interfaces.components.PillButton
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.SkyBlue
import pe.greenminds.ecomind.shared.interfaces.theme.SkyBlueDark
import pe.greenminds.ecomind.shared.interfaces.theme.SunYellow
import pe.greenminds.ecomind.shared.interfaces.theme.SunYellowDark

// Proportion of the tiles in the design (142 x 137)
private const val TILE_ASPECT_RATIO = 142f / 137f

// The top bar and the bottom bar are drawn by MainShell; this is only the center of the screen
@Composable
fun MainMenuScreen(
    onOpenQuests: () -> Unit,
    onOpenLearning: () -> Unit,
    onOpenPlaceholder: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 28.dp)
    ) {
        Spacer(modifier = Modifier.height(28.dp))
        PillButton(
            text = stringResource(R.string.main_menu_energy),
            onClick = onOpenQuests,
            faceColor = SunYellow,
            baseColor = SunYellowDark,
            iconRes = R.drawable.ic_lightbulb
        )

        // The tiles have no destination in the design yet
        Spacer(modifier = Modifier.height(40.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(21.dp)) {
            ImageTileButton(
                defaultRes = R.drawable.img_tile_quests_default,
                pressedRes = R.drawable.img_tile_quests_pressed,
                contentDescription = stringResource(R.string.main_menu_quests),
                onClick = onOpenPlaceholder,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(TILE_ASPECT_RATIO)
            )
            ImageTileButton(
                defaultRes = R.drawable.img_tile_minigames_default,
                pressedRes = R.drawable.img_tile_minigames_pressed,
                contentDescription = stringResource(R.string.main_menu_minigames),
                onClick = onOpenPlaceholder,
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
                onClick = onOpenPlaceholder,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(TILE_ASPECT_RATIO)
            )
            ImageTileButton(
                defaultRes = R.drawable.img_tile_videos_default,
                pressedRes = R.drawable.img_tile_videos_pressed,
                contentDescription = stringResource(R.string.main_menu_videos),
                onClick = onOpenPlaceholder,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(TILE_ASPECT_RATIO)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        IconButton(onClick = onOpenPlaceholder) {
            Image(
                painter = painterResource(R.drawable.ic_more_horiz),
                contentDescription = stringResource(R.string.main_menu_more),
                modifier = Modifier.size(33.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        PillButton(
            text = stringResource(R.string.main_menu_learn_more),
            onClick = onOpenLearning,
            faceColor = SkyBlue,
            baseColor = SkyBlueDark,
            iconRes = R.drawable.ic_lightbulb
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun MainMenuScreenPreview() {
    EcoMindTheme {
        MainMenuScreen(
            onOpenQuests = {},
            onOpenLearning = {},
            onOpenPlaceholder = {}
        )
    }
}
