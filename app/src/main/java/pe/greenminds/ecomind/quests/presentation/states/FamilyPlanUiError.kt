package pe.greenminds.ecomind.quests.presentation.states

import pe.greenminds.ecomind.shared.infrastructure.remote.ApiException

data class FamilyPlanUiError(val code: String?, val message: String) {
    companion object {
        fun from(exception: Throwable) = FamilyPlanUiError(
            (exception as? ApiException)?.code,
            exception.message ?: "Could not load family plans"
        )
    }
}
