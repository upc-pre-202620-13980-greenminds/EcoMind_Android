package pe.greenminds.ecomind.shared.interfaces.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme

@Composable
fun EcoMindLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(R.drawable.img_logo_ecomind),
        contentDescription = stringResource(R.string.logo_content_description),
        contentScale = ContentScale.Fit,
        // Default size of the loading, sign in and sign up screens;
        // a size set by the caller goes first and wins
        modifier = modifier.size(width = 203.dp, height = 74.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun EcoMindLogoPreview() {
    EcoMindTheme {
        EcoMindLogo()
    }
}
