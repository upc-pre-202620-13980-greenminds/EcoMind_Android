package pe.greenminds.ecomind.shared.application

import pe.greenminds.ecomind.shared.domain.model.NotificationCategory
import pe.greenminds.ecomind.shared.domain.repositories.PreferencesRepository
import javax.inject.Inject

class SetNotificationCategoryUseCase @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) {

    suspend operator fun invoke(category: NotificationCategory, enabled: Boolean): Result<Unit> {
        return preferencesRepository.setCategoryEnabled(category, enabled)
    }
}
