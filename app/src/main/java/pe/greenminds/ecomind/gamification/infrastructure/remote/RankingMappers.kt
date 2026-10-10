package pe.greenminds.ecomind.gamification.infrastructure.remote

import pe.greenminds.ecomind.gamification.domain.model.RankingParticipant
import pe.greenminds.ecomind.gamification.domain.model.RankingTransaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// Date and time without fractions of a second: yyyy-MM-ddTHH:mm:ss
private const val INSTANT_PATTERN = "yyyy-MM-dd'T'HH:mm:ss"
private const val INSTANT_LENGTH = 19

fun RankingEntryDto.toDomain(): RankingParticipant {
    return RankingParticipant(
        id = beneficiaryId,
        displayName = displayName,
        totalEcopoints = totalEcopoints.toInt()
    )
}

fun RankingTransactionDto.toDomain(): RankingTransaction {
    return RankingTransaction(
        beneficiaryId = beneficiaryId,
        ecopoints = ecopoints.toInt(),
        occurredAtMillis = instantToMillis(occurredAt)
    )
}

// java.time needs API 26, so the instants of the web services are converted with SimpleDateFormat
fun instantToMillis(instant: String): Long {
    // Only the first 19 characters are read, so fractions of a second do not break the parsing
    val text = instant.take(INSTANT_LENGTH)
    return utcFormat().parse(text)?.time ?: 0L
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
