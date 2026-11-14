package pe.greenminds.ecomind.community.application.usecase

import javax.inject.Inject
import pe.greenminds.ecomind.community.domain.repositories.CommunityPreferenceRepository

class SavePreferredLocalityUseCase @Inject constructor(
    private val repository: CommunityPreferenceRepository
) {
    suspend operator fun invoke(locality: String) {
        require(locality.isNotBlank()) { "The locality is obligatory" }
        repository.savePreferredLocality(locality.trim())
    }
}