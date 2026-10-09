package pe.greenminds.ecomind.gamification.infrastructure.remote

import pe.greenminds.ecomind.gamification.domain.model.RankingParticipant
import pe.greenminds.ecomind.gamification.domain.model.RankingTransaction
import java.text.SimpleDateFormat
import java.util.Date
import java.text.ParsePosition
import java.util.Locale
import java.util.TimeZone

// Date and time without fractions of a second: yyyy-MM-ddTHH:mm:ss
private const val INSTANT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss"

fun RankingEntryDto.toDomain(): RankingParticipant {
    return RankingParticipant(
        id = beneficiaryId,
        displayName = displayName,
        totalEcopoints = totalEcopoints.checkedInt()
    )
}

fun RankingTransactionDto.toDomain(): RankingTransaction {
    return RankingTransaction(
        beneficiaryId = beneficiaryId,
        ecopoints = ecopoints.checkedInt(),
        occurredAtMillis = instantToMillis(occurredAt)
    )
}

// java.time needs API 26, so the instants of the web services are converted with SimpleDateFormat
fun instantToMillis(instant: String): Long {
    val match = Regex("""(\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2})(?:\.(\d{1,9}))?(Z|[+-]\d{2}:\d{2})""").matchEntire(instant)
        ?: error("Invalid server timestamp")
    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US).apply { isLenient = false }
    val text = match.groupValues[1] + match.groupValues[3]
    val position = ParsePosition(0)
    val parsed = format.parse(text, position) ?: error("Invalid server timestamp")
    check(position.index == text.length)
    val millis = match.groupValues[2].padEnd(3, '0').take(3).toLong()
    return parsed.time + millis
}

private fun Long.checkedInt(): Int {
    require(this in 0..Int.MAX_VALUE.toLong()) { "Score is outside the supported range" }
    return toInt()
}

// Used for the from and to parameters of the transactions route
fun millisToInstant(millis: Long): String {
    return utcFormat().format(Date(millis)) + "Z"
}

// A new format for each call: SimpleDateFormat cannot be shared between coroutines
private fun utcFormat(): SimpleDateFormat {
    val format = SimpleDateFormat(INSTANT_PATTERN, Locale.US)
    format.timeZone = TimeZone.getTimeZone("UTC")
    return format
}
