package pe.greenminds.ecomind.gamification.interfaces.ranking

import pe.greenminds.ecomind.gamification.domain.model.Ranking
import pe.greenminds.ecomind.gamification.domain.model.RankingPeriod
import pe.greenminds.ecomind.gamification.domain.model.RankingType

data class RankingUiState(
    val selectedType: RankingType = RankingType.LOCAL,
    val selectedPeriod: RankingPeriod = RankingPeriod.WEEKLY,
    val isLoading: Boolean = true,
    // True when the user has not completed any activity yet: the ranking stays closed
    val isLocked: Boolean = false,
    val hasError: Boolean = false,
    val ranking: Ranking? = null
)
