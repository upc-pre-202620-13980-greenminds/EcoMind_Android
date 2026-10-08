package pe.greenminds.ecomind.gamification.domain.services

import pe.greenminds.ecomind.gamification.domain.model.Ranking
import pe.greenminds.ecomind.gamification.domain.model.RankingEntry
import pe.greenminds.ecomind.gamification.domain.model.RankingParticipant
import pe.greenminds.ecomind.gamification.domain.model.RankingTransaction

// The web services return participants and transactions; the positions are worked out here
object RankingCalculator {

    const val TOP_SIZE = 20

    // Ecopoints each participant earned, adding up their transactions
    fun sumByParticipant(transactions: List<RankingTransaction>): Map<Long, Int> {
        return transactions
            .groupBy { it.beneficiaryId }
            .mapValues { (_, items) -> items.sumOf { it.ecopoints } }
    }

    fun rank(
        participants: List<RankingParticipant>,
        ecopointsById: Map<Long, Int>,
        currentId: Long?
    ): Ranking {
        val ordered = participants
            .map { participant -> participant to (ecopointsById[participant.id] ?: 0) }
            // Only those with at least one ecopoint in the period take part
            .filter { (_, ecopoints) -> ecopoints > 0 }
            // More ecopoints first; a tie is ordered by name
            .sortedWith(
                compareByDescending<Pair<RankingParticipant, Int>> { it.second }
                    .thenBy { it.first.displayName }
            )
            .mapIndexed { index, (participant, ecopoints) ->
                RankingEntry(
                    position = index + 1,
                    participantId = participant.id,
                    displayName = participant.displayName,
                    ecopoints = ecopoints,
                    isCurrentUser = participant.id == currentId
                )
            }

        return Ranking(
            entries = ordered.take(TOP_SIZE),
            // Searched in the whole list: the user can be below the first places
            currentEntry = ordered.find { it.isCurrentUser }
        )
    }
}
