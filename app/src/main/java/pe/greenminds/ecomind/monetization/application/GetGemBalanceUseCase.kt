package pe.greenminds.ecomind.monetization.application

import pe.greenminds.ecomind.monetization.domain.repositories.StoreRepository
import javax.inject.Inject

class GetGemBalanceUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    suspend operator fun invoke(): Result<Int> = repository.getGemBalance()
}
