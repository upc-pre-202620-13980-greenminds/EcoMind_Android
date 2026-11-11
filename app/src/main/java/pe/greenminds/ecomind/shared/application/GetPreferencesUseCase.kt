package pe.greenminds.ecomind.shared.application

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import pe.greenminds.ecomind.shared.domain.model.AppPreferences
import pe.greenminds.ecomind.shared.domain.repositories.PreferencesRepository
import javax.inject.Inject

class GetPreferencesUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {

    // Changes every time the theme or a category changes
    operator fun invoke(): Flow<AppPreferences> {
        return combine(
            preferencesRepository.getTheme(),
            preferencesRepository.getEnabledCategories()
        ) { theme, categories ->
            AppPreferences(
                language = preferencesRepository.getLanguage(),
                theme = theme,
                enabledCategories = categories
            )
        }
    }
}
