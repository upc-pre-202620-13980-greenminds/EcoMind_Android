package pe.greenminds.ecomind.quests.presentation.ui.family

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.quests.presentation.ui.execution.EcoMascot
import pe.greenminds.ecomind.shared.interfaces.theme.White
import pe.greenminds.ecomind.shared.interfaces.theme.interTextStyle

@Composable
fun FamilyQuestStartedScreen(onFinished: () -> Unit) {
    BackHandler(onBack = onFinished)
    LaunchedEffect(Unit) {
        delay(2200)
        onFinished()
    }
    Column(
        Modifier.fillMaxSize().background(White).statusBarsPadding().padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))
        Text(stringResource(R.string.family_quest_started), style = interTextStyle(24, FontWeight.Bold),
            textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(24.dp))
        EcoMascot(false, Modifier.size(240.dp))
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(28.dp))
    }
}
