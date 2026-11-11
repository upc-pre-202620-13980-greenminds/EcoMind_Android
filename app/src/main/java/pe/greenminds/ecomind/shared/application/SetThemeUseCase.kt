package pe.greenminds.ecomind.shared.application

import pe.greenminds.ecomind.shared.domain.model.AppTheme
import pe.greenminds.ecomind.shared.domain.repositories.PreferencesRepository
import javax.inject.Inject

class SetThemeUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {

    suspend operator fun invoke(theme: AppTheme): Result<Unit> {
        return preferencesRepository.setTheme(theme)
    }
}
