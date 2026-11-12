package pe.greenminds.ecomind.community.domain.valueobjects

enum class PostReactionType(val value: String) {
    LIKE("like"),
    LOVE("love"),
    FUNNY("funny"),
    SURPRISE("surprise"),
    SAD("sad"),
    ANGRY("angry");

    companion object {
        fun fromValue(value: String): PostReactionType =
            entries.first { it.value.equals(value, ignoreCase = true) }
    }
}