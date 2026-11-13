package pe.greenminds.ecomind.community.application.usecase

import javax.inject.Inject
import pe.greenminds.ecomind.community.domain.repositories.CommunityPreferenceRepository

class GetPreferredLocalityUseCase @Inject constructor(
    private val repository: CommunityPreferenceRepository
) {
    suspend operator fun invoke(): String? = repository.getPreferredLocality()
}