package pe.greenminds.ecomind.users.interfaces.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.components.PillButton
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreen
import pe.greenminds.ecomind.shared.interfaces.theme.EcoGreenDark
import pe.greenminds.ecomind.shared.interfaces.theme.TextSecondary
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle

// Shown when the profile could not be loaded; the arrow and the button request it again
@Composable
fun ProfileErrorContent(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backDescription = stringResource(R.string.back)

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
        ) {
            TextLink(
                text = stringResource(R.string.profile_error_back),
                onClick = onRetry,
                style = interTextStyle(24, FontWeight.Bold),
                // The screen reader says "Back" instead of reading the symbol
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .clearAndSetSemantics { contentDescription = backDescription }
            )
            Text(
                text = stringResource(R.string.profile_title),
                style = interTextStyle(20, FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                // The screen reader announces the error as soon as it appears
                modifier = Modifier
                    .align(Alignment.Center)
                    .semantics { liveRegion = LiveRegionMode.Polite }
            ) {
                Text(
                    text = stringResource(R.string.profile_error_title),
                    style = interTextStyle(19, FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { heading() }
                )
                Text(
                    text = stringResource(R.string.profile_error_message),
                    style = interTextStyle(13),
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            PillButton(
                text = stringResource(R.string.profile_try_again),
                onClick = onRetry,
                faceColor = EcoGreen,
                baseColor = EcoGreenDark,
                height = 48.dp,
                textStyle = interTextStyle(16, FontWeight.Bold),
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
