package pe.greenminds.ecomind.settings.application

import pe.greenminds.ecomind.settings.domain.repositories.SettingsRepository
import javax.inject.Inject

class ObservePreferencesUseCase @Inject constructor(private val repository: SettingsRepository) {
    operator fun invoke() = repository.observe()
}
