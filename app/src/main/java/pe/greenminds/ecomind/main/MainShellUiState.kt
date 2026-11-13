package pe.greenminds.ecomind.main

// Counters shown in the top bar of every section
data class MainShellUiState(
    val gemBalance: Int = 0,
    val streak: Int = 0,
    // Number on the bell of the top bar
    val unreadNotifications: Int = 0
)
