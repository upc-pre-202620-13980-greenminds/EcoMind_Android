package pe.greenminds.ecomind.shared.interfaces.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle

// Circle with the initials of a person, used until the real avatars are exported
@Composable
fun InitialsAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
            // Decorative: the name is always written next to the avatar
            .clearAndSetSemantics { }
    ) {
        Text(
            text = initialsOf(name),
            style = interTextStyle(sizeSp = (size.value / 2.6f).toInt(), weight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

// First letter of the first two words: "Luna Rivera" gives "LR"
private fun initialsOf(name: String): String {
    return name.trim()
        .split(" ")
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
}

@Preview(showBackground = true)
@Composable
private fun InitialsAvatarPreview() {
    EcoMindTheme {
        InitialsAvatar(
            name = stringResource(R.string.app_name),
            size = 76.dp,
            modifier = Modifier.padding(16.dp)
        )
    }
}
