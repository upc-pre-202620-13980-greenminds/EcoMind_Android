package pe.greenminds.ecomind.monetization.interfaces.store

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.monetization.domain.model.Multiplier
import pe.greenminds.ecomind.monetization.domain.model.StreakProtector
import pe.greenminds.ecomind.shared.interfaces.theme.Black
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreen
import pe.greenminds.ecomind.shared.interfaces.theme.White
import pe.greenminds.ecomind.shared.interfaces.theme.poppinsTextStyle

@Composable
fun MultiplierCard(
    multiplier: Multiplier,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFeatured = multiplier.factor == 3.0
    val cardColor = when (multiplier.factor) {
        2.0 -> Color(0xFFEFFFED)
        3.0 -> Color(0xFF69A44E)
        else -> White
    }

    BoostCard(
        name = multiplier.name,
        description = multiplier.description,
        priceInGems = multiplier.priceInGems,
        imageResource = boostImage(multiplier.imageReference),
        backgroundColor = cardColor,
        contentColor = if (isFeatured) White else Black,
        buttonColor = if (isFeatured) White else EcoGreen,
        buttonTextColor = if (isFeatured) Color(0xFF396B2B) else White,
        onBuy = onBuy,
        modifier = modifier
    )
}

@Composable
fun StreakProtectorCard(
    protector: StreakProtector,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoostCard(
        name = protector.name,
        description = protector.description,
        priceInGems = protector.priceInGems,
        imageResource = boostImage(protector.imageReference),
        backgroundColor = Color(0xFFE2F3FF),
        contentColor = Black,
        buttonColor = EcoGreen,
        buttonTextColor = White,
        onBuy = onBuy,
        modifier = modifier
    )
}

@Composable
private fun BoostCard(
    name: String,
    description: String,
    priceInGems: Int,
    @DrawableRes imageResource: Int,
    backgroundColor: Color,
    contentColor: Color,
    buttonColor: Color,
    buttonTextColor: Color,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .fillMaxWidth()
            .shadow(3.dp, shape)
            .background(backgroundColor, shape)
            .padding(start = 16.dp, end = 12.dp, top = 10.dp, bottom = 10.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = poppinsTextStyle(15, FontWeight.Bold),
                color = contentColor
            )
            Text(
                text = description,
                style = poppinsTextStyle(8, FontWeight.Normal),
                color = contentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            PriceRow(
                priceInGems = priceInGems,
                textColor = if (backgroundColor == Color(0xFF69A44E)) White else null
            )
            ActionButton(
                text = stringResource(R.string.store_action_buy),
                color = buttonColor,
                textColor = buttonTextColor,
                onClick = onBuy
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
        Image(
            painter = painterResource(imageResource),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(92.dp)
        )
    }
}

@DrawableRes
private fun boostImage(reference: String): Int {
    return when (reference) {
        "world_happy" -> R.drawable.world_happy
        "world_run" -> R.drawable.world_run
        "world_trophy" -> R.drawable.world_trophy
        "world_streak_protector" -> R.drawable.world_streak_protector
        else -> R.drawable.ic_launcher_foreground
    }
}
