package pe.greenminds.ecomind.gamification.interfaces.progress

sealed interface LoadState<out T> {
    data object Loading : LoadState<Nothing>
    data class Loaded<T>(val value: T) : LoadState<T>
    data class Failed(val sessionRequired: Boolean) : LoadState<Nothing>
}
