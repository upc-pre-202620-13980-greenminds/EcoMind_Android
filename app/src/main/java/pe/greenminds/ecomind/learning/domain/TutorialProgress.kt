package pe.greenminds.ecomind.learning.domain

data class TutorialProgress(
    val currentStep: Int,
    val totalSteps: Int,
    val completed: Boolean
)
