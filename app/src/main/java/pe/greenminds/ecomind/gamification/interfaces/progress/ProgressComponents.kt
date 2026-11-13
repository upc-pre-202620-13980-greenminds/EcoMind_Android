package pe.greenminds.ecomind.gamification.interfaces.progress

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import pe.greenminds.ecomind.gamification.interfaces.achievements.LoadingContent
import pe.greenminds.ecomind.gamification.interfaces.achievements.RecoveryContent

@Composable
internal fun ColumnScope.ProgressContent(
    isLoading: Boolean,
    hasError: Boolean,
    sessionRequired: Boolean,
    onRetry: () -> Unit,
    onSignIn: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    when {
        isLoading -> LoadingContent()
        hasError -> RecoveryContent(sessionRequired, onRetry, onSignIn)
        else -> content()
    }
}
