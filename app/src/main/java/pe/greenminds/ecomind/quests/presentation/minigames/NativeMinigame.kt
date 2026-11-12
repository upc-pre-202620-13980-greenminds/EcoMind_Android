package pe.greenminds.ecomind.quests.presentation.minigames

internal enum class NativeMinigame {
    COLLECT_WASTE;

    companion object {
        // The paths identify native games across environments, where numeric IDs can differ.
        fun fromUrl(url: String): NativeMinigame? = when (url) {
            "/games/collect-waste", "/games/recoge-residuos" -> COLLECT_WASTE
            else -> null
        }
    }
}
