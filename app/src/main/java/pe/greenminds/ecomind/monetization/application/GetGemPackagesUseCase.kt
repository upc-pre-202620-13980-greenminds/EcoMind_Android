package pe.greenminds.ecomind.monetization.application

import pe.greenminds.ecomind.monetization.domain.model.GemPackage
import pe.greenminds.ecomind.monetization.domain.repositories.StoreRepository
import javax.inject.Inject

class GetGemPackagesUseCase @Inject constructor(
    private val repository: StoreRepository
) {
    suspend operator fun invoke(): Result<List<GemPackage>> = repository.getGemPackages()
}
