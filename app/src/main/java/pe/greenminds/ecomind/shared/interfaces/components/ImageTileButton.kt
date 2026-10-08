package pe.greenminds.ecomind.shared.interfaces.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme

// Button drawn with an image that changes while it is being pressed
@Composable
fun ImageTileButton(
    @DrawableRes defaultRes: Int,
    @DrawableRes pressedRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Image(
        painter = painterResource(if (isPressed) pressedRes else defaultRes),
        contentDescription = contentDescription,
        contentScale = ContentScale.Fit,
        modifier = modifier.clickable(
            interactionSource = interactionSource,
            // The pressed image is the visual feedback, so the ripple is not needed
            indication = null,
            role = Role.Button,
            onClick = onClick
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun ImageTileButtonPreview() {
    EcoMindTheme {
        ImageTileButton(
            defaultRes = R.drawable.img_tile_quests_default,
            pressedRes = R.drawable.img_tile_quests_pressed,
            contentDescription = stringResource(R.string.main_menu_quests),
            onClick = {},
            modifier = Modifier
                .padding(16.dp)
                .size(width = 142.dp, height = 137.dp)
        )
    }
}
