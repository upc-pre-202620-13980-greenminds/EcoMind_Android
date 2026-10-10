package pe.greenminds.ecomind.settings.infrastructure.local

import androidx.datastore.preferences.core.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.settings.domain.model.*
import pe.greenminds.ecomind.settings.domain.repositories.SettingsRepository
import pe.greenminds.ecomind.shared.infrastructure.ExperienceDataStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalSettingsRepository @Inject constructor(
    private val store: ExperienceDataStore,
    private val sessions: SessionRepository
) : SettingsRepository {
    private val languageKey = stringPreferencesKey("language")
    private val themeKey = stringPreferencesKey("theme")
    override fun observe(): Flow<AppPreferences> = combine(store.dataStore.data, sessions.getSession()) { values, session ->
        AppPreferences(
            AppLanguage.entries.find { it.name == values[languageKey] } ?: AppLanguage.ENGLISH,
            AppTheme.entries.find { it.name == values[themeKey] } ?: AppTheme.LIGHT,
            NotificationCategory.entries.filter { category ->
                values[booleanPreferencesKey("notifications_${session?.accountId}_${category.name}")]
                    ?: (category != NotificationCategory.WEEKLY_SUMMARY)
            }.toSet()
        )
    }
    override suspend fun setLanguage(value: AppLanguage) = write { it[languageKey] = value.name }
    override suspend fun setTheme(value: AppTheme) = write { it[themeKey] = value.name }
    override suspend fun setNotification(category: NotificationCategory, enabled: Boolean): Result<Unit> {
        val session = sessions.getSession().first()
        if (session == null || session.isExpired(System.currentTimeMillis())) return Result.failure(IllegalStateException("Session required"))
        return write { it[booleanPreferencesKey("notifications_${session.accountId}_${category.name}")] = enabled }
    }
    private suspend fun write(block: (MutablePreferences) -> Unit): Result<Unit> = try {
        store.dataStore.edit(block)
        Result.success(Unit)
    } catch (cancelled: CancellationException) { throw cancelled }
      catch (exception: Exception) { Result.failure(exception) }
}
