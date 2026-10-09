package pe.greenminds.ecomind.shared.interfaces.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.SunYellow
import pe.greenminds.ecomind.shared.interfaces.theme.SunYellowDark
import pe.greenminds.ecomind.shared.interfaces.theme.White

// Rounded button with a darker base under the face, which gives it volume
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    faceColor: Color,
    baseColor: Color,
    modifier: Modifier = Modifier,
    @DrawableRes iconRes: Int? = null,
    height: Dp = 63.dp,
    textStyle: TextStyle = MaterialTheme.typography.headlineSmall,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(percent = 50)
    // The base and the shine keep the same proportion in the large and the small button.
    val depth = height * 0.1f
    val shineHeight = height * 0.18f
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick
            )
    ) {
        // Base: what is seen under the face
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(top = depth)
                .background(baseColor, shape)
        )
        // Face
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(bottom = depth)
                .offset(y = if (isPressed) depth else 0.dp)
                .clip(shape)
                .background(faceColor)
        ) {
            // The light layer is another rounded face behind the main face. Its curved
            // edge remains visible at the top and disappears naturally down the sides.
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(White.copy(alpha = 0.35f))
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(y = shineHeight)
                    .clip(shape)
                    .background(faceColor)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    text = text,
                    color = White,
                    maxLines = 1,
                    style = textStyle.copy(
                        shadow = Shadow(
                            color = baseColor,
                            offset = Offset(0f, 4f),
                            blurRadius = 4f
                        )
                    )
                )
                if (iconRes != null) {
                    Spacer(modifier = Modifier.width(18.dp))
                    // Decorative: the text already says what the button does
                    Image(
                        painter = painterResource(iconRes),
                        contentDescription = null,
                        modifier = Modifier.size(33.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PillButtonPreview() {
    EcoMindTheme {
        PillButton(
            text = stringResource(R.string.main_menu_energy),
            onClick = {},
            faceColor = SunYellow,
            baseColor = SunYellowDark,
            iconRes = R.drawable.ic_lightbulb,
            modifier = Modifier.padding(16.dp)
        )
    }
}
