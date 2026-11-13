package pe.greenminds.ecomind.community.application.usecase

import javax.inject.Inject
import pe.greenminds.ecomind.community.domain.repositories.CommunityLocationRepository

class GetCurrentLocalityUseCase @Inject constructor(
    private val repository: CommunityLocationRepository
) {
    suspend operator fun invoke(): Result<String> = repository.getCurrentLocality()
}