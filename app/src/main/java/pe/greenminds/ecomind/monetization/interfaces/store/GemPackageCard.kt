package pe.greenminds.ecomind.monetization.interfaces.store

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.monetization.domain.model.GemPackage
import pe.greenminds.ecomind.shared.interfaces.theme.Black
import pe.greenminds.ecomind.shared.interfaces.theme.SkyBlue
import pe.greenminds.ecomind.shared.interfaces.theme.White
import pe.greenminds.ecomind.shared.interfaces.theme.poppinsTextStyle

@Composable
fun GemPackageCard(
    gemPackage: GemPackage,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .shadow(3.dp, shape)
            .background(White, shape)
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        Image(
            painter = painterResource(gemPackageImage(gemPackage.imageReference)),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(62.dp)
        )
        Text(
            text = stringResource(R.string.store_gem_amount, gemPackage.gemAmount),
            style = poppinsTextStyle(11, FontWeight.SemiBold),
            color = SkyBlue,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.store_gem_price, gemPackage.price),
            style = poppinsTextStyle(10, FontWeight.SemiBold),
            color = Black
        )
        Spacer(modifier = Modifier.height(4.dp))
        ActionButton(
            text = stringResource(R.string.store_action_buy),
            color = SkyBlue,
            onClick = onBuy
        )
    }
}

@Composable
fun MegaGemPackageCard(
    gemPackage: GemPackage,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 160.dp)
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF35D8E3), Color(0xFF0873C7))
                ),
                shape = shape
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth(0.55f)
                .heightIn(min = 136.dp)
                .align(Alignment.CenterStart)
        ) {
            Text(
                text = stringResource(R.string.store_mega_bundle),
                style = poppinsTextStyle(16, FontWeight.Bold),
                color = White
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "• ${stringResource(R.string.store_gem_amount, gemPackage.gemAmount)}",
                style = poppinsTextStyle(11, FontWeight.Normal),
                color = White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "• ${stringResource(R.string.store_mega_avatars)}",
                style = poppinsTextStyle(11, FontWeight.Normal),
                color = White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "• ${stringResource(R.string.store_mega_multiplier)}",
                style = poppinsTextStyle(11, FontWeight.Normal),
                color = White
            )
        }

        Text(
            text = stringResource(R.string.store_gem_price, gemPackage.price),
            style = poppinsTextStyle(16, FontWeight.Bold),
            color = White,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(x = 22.dp, y = 36.dp)
        )

        Image(
            painter = painterResource(gemPackageImage(gemPackage.imageReference)),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(156.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 34.dp, y = 6.dp)
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 20.dp, y = (-6).dp)
        ) {
            ActionButton(
                text = stringResource(R.string.store_action_buy),
                color = White,
                textColor = SkyBlue,
                onClick = onBuy
            )
        }
    }
}

@DrawableRes
private fun gemPackageImage(reference: String): Int {
    return when (reference) {
        "gem_pack_0" -> R.drawable.gem_pack_0
        "gem_pack_1" -> R.drawable.gem_pack_1
        "gem_pack_2" -> R.drawable.gem_pack_2
        "gem_pack_3" -> R.drawable.gem_pack_3
        "gem_pack_4" -> R.drawable.gem_pack_4
        else -> R.drawable.ic_gem
    }
}
