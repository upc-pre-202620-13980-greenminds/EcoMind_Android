package pe.greenminds.ecomind.monetization.application

import pe.greenminds.ecomind.monetization.domain.model.Multiplier
import pe.greenminds.ecomind.monetization.domain.model.StreakProtector
import pe.greenminds.ecomind.monetization.domain.repositories.StoreRepository
import javax.inject.Inject

data class BoostItems(
    val multipliers: List<Multiplier>,
    val protectors: List<StreakProtector>
)

class GetBoostItemsUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    suspend operator fun invoke(): Result<BoostItems> {
        val multipliers = repository.getMultipliers()
            .getOrElse { return Result.failure(it) }
        val protectors = repository.getStreakProtectors()
            .getOrElse { return Result.failure(it) }

        return Result.success(BoostItems(multipliers, protectors))
    }
}
