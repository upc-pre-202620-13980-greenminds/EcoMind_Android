package pe.greenminds.ecomind.shared.application

import pe.greenminds.ecomind.shared.domain.model.AppLanguage
import pe.greenminds.ecomind.shared.domain.repositories.PreferencesRepository
import javax.inject.Inject

class SetLanguageUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {

    operator fun invoke(language: AppLanguage) {
        if (preferencesRepository.getLanguage() != language) {
            preferencesRepository.setLanguage(language)
        }
    }
}
