package pe.greenminds.ecomind.quests.domain.entity

data class ActivityUser (
    val id:Long,
    val questUserId: Long,
    val activityId: Long,
    val progress: Double,
    val endDate: String?,
    val description: String,
    val configuration: Map<String, Any>,
    val collaborativeSessionId: Long? = null,
    val responseText: String? = null
) {
    val minCharacters: Int
        get() = (configuration["minCharacters"] as? Number)?.toInt()?.coerceAtLeast(1) ?: 1

    fun acceptsWrittenResponse(text: String): Boolean {
        val trimmed = text.trim { Character.isWhitespace(it) }
        return trimmed.codePointCount(0, trimmed.length) >= minCharacters
    }
}
