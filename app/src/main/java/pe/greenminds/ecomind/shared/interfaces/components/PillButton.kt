package pe.greenminds.ecomind.shared.interfaces.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
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
    @DrawableRes iconRes: Int? = null
) {
    val shape = RoundedCornerShape(percent = 50)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(63.dp)
            .shadow(elevation = 3.dp, shape = shape)
            .clip(shape)
            .clickable(role = Role.Button, onClick = onClick)
    ) {
        // Base: what is seen under the face
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(top = 6.dp)
                .background(baseColor, shape)
        )
        // Face
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(bottom = 6.dp)
                .clip(shape)
                .background(faceColor)
        ) {
            // Shine on the upper edge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .background(White.copy(alpha = 0.35f))
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.align(Alignment.Center)
            ) {
                Text(
                    text = text,
                    color = White,
                    maxLines = 1,
                    style = MaterialTheme.typography.headlineSmall.copy(
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
