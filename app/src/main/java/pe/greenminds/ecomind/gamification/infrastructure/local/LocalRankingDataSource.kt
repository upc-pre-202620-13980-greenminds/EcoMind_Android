package pe.greenminds.ecomind.gamification.infrastructure.local

import pe.greenminds.ecomind.gamification.domain.model.RankingPeriod
import pe.greenminds.ecomind.gamification.domain.model.RankingType
import pe.greenminds.ecomind.gamification.domain.services.RankingPeriodRange
import pe.greenminds.ecomind.gamification.infrastructure.remote.RankingEntryDto
import pe.greenminds.ecomind.gamification.infrastructure.remote.RankingPageDto
import pe.greenminds.ecomind.gamification.infrastructure.remote.RankingTransactionDto
import pe.greenminds.ecomind.gamification.infrastructure.remote.instantToMillis
import pe.greenminds.ecomind.gamification.infrastructure.remote.millisToInstant
import javax.inject.Inject

// Sample data with the shape of the responses of the web services (Gamification, provisional)
class LocalRankingDataSource @Inject constructor() {

    // Ecopoints earned today, earlier this week, earlier this month and before that
    private data class Sample(
        val id: Long,
        val name: String,
        val today: Int,
        val week: Int,
        val month: Int,
        val older: Int
    )

    companion object {
        private const val MINUTE_MILLIS = 60 * 1000L
        private const val DAY_MILLIS = 24 * 60 * MINUTE_MILLIS
    }

    // Ids 1 to 7 are the same sample users of the Users context
    private val people = listOf(
        Sample(1L, "Camila Torres", today = 6, week = 10, month = 0, older = 0),
        Sample(2L, "Robin Green", today = 6, week = 10, month = 0, older = 0),
        Sample(3L, "Luna Rivera", today = 10, week = 24, month = 47, older = 30),
        Sample(4L, "Sal Torres", today = 5, week = 14, month = 28, older = 20),
        Sample(5L, "Diego Ramos", today = 4, week = 10, month = 38, older = 15),
        Sample(6L, "Valeria Chávez", today = 3, week = 8, month = 28, older = 10),
        Sample(7L, "Mateo Vargas", today = 9, week = 20, month = 59, older = 40),
        Sample(8L, "Sofía Quispe", today = 7, week = 18, month = 45, older = 25),
        Sample(9L, "Ulices Torres", today = 2, week = 7, month = 21, older = 12),
        Sample(10L, "Mia Flores", today = 0, week = 0, month = 22, older = 5),
        Sample(11L, "Camila Rojas", today = 15, week = 37, month = 60, older = 80),
        Sample(12L, "Bruno Salazar", today = 9, week = 24, month = 40, older = 30),
        Sample(13L, "Renata Díaz", today = 8, week = 20, month = 35, older = 20),
        Sample(14L, "Lucas Paredes", today = 6, week = 18, month = 30, older = 22),
        Sample(15L, "Emma Castillo", today = 5, week = 17, month = 25, older = 18)
    )

    // Family 1 is the sample family of the Users context
    private val families = listOf(
        Sample(1L, "Green Home", today = 12, week = 35, month = 60, older = 40),
        Sample(2L, "Rivera Family", today = 20, week = 44, month = 70, older = 50),
        Sample(3L, "Eco Quispe", today = 18, week = 40, month = 66, older = 45),
        Sample(4L, "Torres Family", today = 12, week = 27, month = 50, older = 30),
        Sample(5L, "Casa Verde", today = 9, week = 22, month = 41, older = 28),
        Sample(6L, "Vargas Family", today = 8, week = 18, month = 36, older = 20),
        Sample(7L, "Los Ramos", today = 6, week = 14, month = 30, older = 16),
        Sample(8L, "Chávez Family", today = 5, week = 10, month = 24, older = 12)
    )

    private val localIds = (1L..10L).toList()

    // Accepted friends of the two demo accounts
    private val friendIds = mapOf(
        1L to listOf(3L, 4L),
        2L to listOf(6L, 7L)
    )

    // userId plays the role of the access token: the web services know who is asking
    fun participantsPage(
        type: RankingType,
        userId: Long,
        page: Int,
        size: Int
    ): RankingPageDto<RankingEntryDto> {
        val entries = samplesOf(type, userId).map { sample ->
            RankingEntryDto(
                beneficiaryId = sample.id,
                displayName = sample.name,
                totalEcopoints = (sample.today + sample.week + sample.month + sample.older).toLong()
            )
        }
        return pageOf(entries, page, size)
    }

    fun transactionsPage(
        type: RankingType,
        userId: Long,
        from: String,
        to: String,
        page: Int,
        size: Int
    ): RankingPageDto<RankingTransactionDto> {
        val fromMillis = instantToMillis(from)
        val toMillis = instantToMillis(to)

        val transactions = samplesOf(type, userId)
            .flatMap { sample -> transactionsOf(sample, nowMillis = System.currentTimeMillis()) }
            .filter { dto ->
                val occurredAt = instantToMillis(dto.occurredAt)
                occurredAt in fromMillis until toMillis
            }
        return pageOf(transactions, page, size)
    }

    private fun samplesOf(type: RankingType, userId: Long): List<Sample> {
        return when (type) {
            RankingType.LOCAL -> people.filter { it.id in localIds }
            RankingType.GLOBAL -> people
            // The user and their accepted friends
            RankingType.FRIENDS -> {
                val ids = listOf(userId) + (friendIds[userId] ?: emptyList())
                people.filter { it.id in ids }
            }

            RankingType.FAMILIES -> families
        }
    }

    // The dates are relative to now, so every period always has something to show.
    // At the very start of a week or a month some amounts fall in the same period.
    private fun transactionsOf(sample: Sample, nowMillis: Long): List<RankingTransactionDto> {
        val weekStart = RankingPeriodRange.startOf(RankingPeriod.WEEKLY, nowMillis) ?: nowMillis
        val monthStart = RankingPeriodRange.startOf(RankingPeriod.MONTHLY, nowMillis) ?: nowMillis

        val amounts = listOf(
            sample.today to nowMillis - MINUTE_MILLIS,
            sample.week to weekStart + MINUTE_MILLIS,
            sample.month to monthStart + MINUTE_MILLIS,
            sample.older to nowMillis - 60 * DAY_MILLIS
        )
        return amounts
            .filter { (ecopoints, _) -> ecopoints > 0 }
            .mapIndexed { index, (ecopoints, occurredAt) ->
                RankingTransactionDto(
                    id = "sample-${sample.id}-$index",
                    beneficiaryId = sample.id,
                    ecopoints = ecopoints.toLong(),
                    occurredAt = millisToInstant(occurredAt)
                )
            }
    }

    private fun <T> pageOf(all: List<T>, page: Int, size: Int): RankingPageDto<T> {
        val start = page * size
        val items = all.drop(start).take(size)
        return RankingPageDto(
            items = items,
            page = page,
            size = size,
            hasNext = start + size < all.size
        )
    }
}
