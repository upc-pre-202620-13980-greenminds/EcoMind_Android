package pe.greenminds.ecomind.monetization.interfaces.store

import androidx.annotation.StringRes
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.monetization.domain.model.Cosmetic
import pe.greenminds.ecomind.monetization.domain.model.CosmeticOwnership
import pe.greenminds.ecomind.monetization.domain.model.CosmeticType
import pe.greenminds.ecomind.monetization.domain.model.StoreItem
import pe.greenminds.ecomind.shared.interfaces.theme.Black
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreen
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.ForestGreen
import pe.greenminds.ecomind.shared.interfaces.theme.LightGrayText
import pe.greenminds.ecomind.shared.interfaces.theme.MintTint
import pe.greenminds.ecomind.shared.interfaces.theme.MutedGray
import pe.greenminds.ecomind.shared.interfaces.theme.SkyBlue
import pe.greenminds.ecomind.shared.interfaces.theme.White
import pe.greenminds.ecomind.shared.interfaces.theme.poppinsTextStyle

// One cosmetic of the store. Its look depends on what it is for the user:
// not owned (price and "Buy"), in the inventory ("Equip") or equipped ("Unequip").
@Composable
fun StoreItemCard(
    item: StoreItem,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(10.dp)
    val isOwned = item.ownership != CosmeticOwnership.NOT_OWNED

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isOwned) {
                    // Owned cosmetics have a tinted card with a green border
                    val borderColor =
                        if (item.ownership == CosmeticOwnership.EQUIPPED) ForestGreen else EcoGreen
                    Modifier
                        .background(MintTint, shape)
                        .border(1.dp, borderColor, shape)
                } else {
                    // Cosmetics for sale have a white card with a shadow
                    Modifier
                        .shadow(elevation = 3.dp, shape = shape)
                        .background(White, shape)
                }
            )
    ) {
        if (item.ownership == CosmeticOwnership.IN_INVENTORY) {
            OwnershipBadge(
                text = stringResource(R.string.store_badge_in_inventory),
                color = SkyBlue,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
        if (item.ownership == CosmeticOwnership.EQUIPPED) {
            OwnershipBadge(
                text = stringResource(R.string.store_badge_equipped),
                color = ForestGreen,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 28.dp, bottom = 4.dp)
        ) {
            Image(
                painter = painterResource(cosmeticImageResource(item.cosmetic.imageReference)),
                contentDescription = item.cosmetic.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(width = 86.dp, height = 90.dp)
                    .then(
                        if (item.cosmetic.type == CosmeticType.HEAD) {
                            Modifier.offset { IntOffset(0, -6.dp.roundToPx()) }
                        } else {
                            Modifier
                        }
                    )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.cosmetic.name,
                style = poppinsTextStyle(10, FontWeight.SemiBold),
                color = Black,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(typeLabel(item.cosmetic.type)),
                style = poppinsTextStyle(10, FontWeight.Light),
                color = LightGrayText
            )

            if (!isOwned) {
                PriceRow(priceInGems = item.cosmetic.priceInGems)
            } else {
                // Preserve the price-row space so buying an item does not resize its card.
                Spacer(modifier = Modifier.height(26.dp))
            }

            ActionButton(
                text = stringResource(actionLabel(item.ownership)),
                color = if (item.ownership == CosmeticOwnership.EQUIPPED) MutedGray else EcoGreen,
                onClick = onAction
            )
        }
    }
}

@DrawableRes
internal fun cosmeticImageResource(reference: String?): Int = when (reference) {
    "avatar_alex" -> R.drawable.avatar_alex
    "avatar_leafwings" -> R.drawable.avatar_leafwings
    "avatar_lunalunette" -> R.drawable.avatar_lunalunette
    "avatar_mickey" -> R.drawable.avatar_mickey
    "avatar_ranma" -> R.drawable.avatar_ranma
    "avatar_rosalina" -> R.drawable.avatar_rosalina
    "avatar_salsolcito" -> R.drawable.avatar_salsolcito
    "avatar_sonic" -> R.drawable.avatar_sonic
    "cosmetic_bun" -> R.drawable.cosmetic_bun
    "cosmetic_observatorio" -> R.drawable.cosmetic_observatorio
    else -> R.drawable.cosmetic_hat
}

@Composable
private fun OwnershipBadge(text: String, color: Color, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = poppinsTextStyle(10, FontWeight.Medium),
        color = White,
        maxLines = 1,
        modifier = modifier
            .padding(top = 6.dp, end = 6.dp)
            .background(color, RoundedCornerShape(percent = 50))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

@Composable
internal fun PriceRow(priceInGems: Int, textColor: Color? = null) {
    val description = stringResource(R.string.top_bar_gems, priceInGems)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        // The screen reader says "100 gems" instead of reading icon and number apart
        modifier = Modifier
            .padding(top = 4.dp)
            .clearAndSetSemantics { contentDescription = description }
    ) {
        Image(
            painter = painterResource(R.drawable.ic_gem),
            contentDescription = null,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = priceInGems.toString(),
            style = poppinsTextStyle(10, FontWeight.SemiBold),
            color = textColor ?: SkyBlue
        )
    }
}

// The pill is small as in the design; the box around it keeps a touch target of 48dp
@Composable
internal fun ActionButton(
    text: String,
    color: Color,
    onClick: () -> Unit,
    textColor: Color = White
) {
    val pillShape = RoundedCornerShape(percent = 50)

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
            .clip(pillShape)
            .clickable(role = Role.Button, onClick = onClick)
    ) {
        Text(
            text = text,
            style = poppinsTextStyle(10, FontWeight.Bold),
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .heightIn(min = 24.dp)
                .background(color, pillShape)
                .padding(horizontal = 16.dp, vertical = 5.dp)
        )
    }
}

@StringRes
private fun typeLabel(type: CosmeticType): Int {
    return when (type) {
        CosmeticType.AVATAR -> R.string.store_type_avatar
        CosmeticType.HEAD -> R.string.store_type_head
        CosmeticType.BODY -> R.string.store_type_body
        CosmeticType.ACCESSORY -> R.string.store_type_accessory
    }
}

@StringRes
private fun actionLabel(ownership: CosmeticOwnership): Int {
    return when (ownership) {
        CosmeticOwnership.NOT_OWNED -> R.string.store_action_buy
        CosmeticOwnership.IN_INVENTORY -> R.string.store_action_equip
        CosmeticOwnership.EQUIPPED -> R.string.store_action_unequip
    }
}

@Preview(showBackground = true, widthDp = 180)
@Composable
private fun StoreItemCardPreview() {
    EcoMindTheme {
        StoreItemCard(
            item = StoreItem(
                cosmetic = Cosmetic(
                    id = "1",
                    name = stringResource(R.string.app_name),
                    description = "",
                    priceInGems = 100,
                    type = CosmeticType.AVATAR,
                    imageReference = null
                ),
                ownership = CosmeticOwnership.NOT_OWNED
            ),
            onAction = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
