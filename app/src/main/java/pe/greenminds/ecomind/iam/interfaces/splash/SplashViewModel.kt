package pe.greenminds.ecomind.iam.interfaces.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.iam.application.CheckSessionUseCase
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val checkSession: CheckSessionUseCase
) : ViewModel() {

    companion object {
        // Reading the session takes a few milliseconds; without this the logo would not be seen
        private const val MIN_VISIBLE_MILLIS = 1000L
    }

    private val _state = MutableStateFlow(SplashUiState())
    val state: StateFlow<SplashUiState> = _state.asStateFlow()

    init {
        decideDestination()
    }

    private fun decideDestination() {
        viewModelScope.launch {
            val hasSession = checkSession()
            delay(MIN_VISIBLE_MILLIS)

            _state.update { currentState ->
                currentState.copy(
                    destination = if (hasSession) {
                        SplashDestination.MAIN_MENU
                    } else {
                        SplashDestination.SIGN_IN
                    }
                )
            }
        }
    }
}
