package pe.greenminds.ecomind.iam.interfaces.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pe.greenminds.ecomind.shared.interfaces.components.EcoMindLogo
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme

@Composable
fun SplashScreen(
    onNavigateToSignIn: () -> Unit,
    onNavigateToMainMenu: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle().value

    LaunchedEffect(state.destination) {
        when (state.destination) {
            SplashDestination.SIGN_IN -> onNavigateToSignIn()
            SplashDestination.MAIN_MENU -> onNavigateToMainMenu()
            null -> Unit
        }
    }

    SplashContent()
}

// Separated from the ViewModel so it can be shown in the preview
@Composable
private fun SplashContent(modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        EcoMindLogo()
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun SplashContentPreview() {
    EcoMindTheme {
        SplashContent()
    }
}
