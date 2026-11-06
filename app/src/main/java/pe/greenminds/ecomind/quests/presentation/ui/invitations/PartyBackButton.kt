package pe.greenminds.ecomind.quests.presentation.ui.invitations

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.shared.interfaces.theme.TextPrimary

@Composable
internal fun PartyBackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onBack, modifier = modifier) {
        Icon(
            painter = painterResource(R.drawable.ic_chevron_back),
            contentDescription = stringResource(R.string.quest_invitations_back),
            tint = TextPrimary
        )
    }
}
