package pe.greenminds.ecomind.shared.infrastructure.local

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.greenminds.ecomind.shared.domain.model.AppLanguage
import pe.greenminds.ecomind.shared.domain.model.AppTheme
import pe.greenminds.ecomind.shared.domain.model.NotificationCategory
import pe.greenminds.ecomind.shared.domain.repositories.PreferencesRepository
import java.util.Locale
import javax.inject.Inject

// Its own file, so closing the session does not reset the preferences
private val Context.preferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "preferences"
)

// The web services have no route for preferences (TS-008 asks to keep them in the database),
// so they stay on the device behind the interface until that route exists
class LocalPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : PreferencesRepository {

    companion object {
        private val THEME = stringPreferencesKey("theme")
    }

    override fun getTheme(): Flow<AppTheme> {
        return context.preferencesDataStore.data.map { preferences ->
            preferences[THEME]?.let { stored -> AppTheme.entries.find { it.name == stored } }
                ?: AppTheme.LIGHT
        }
    }

    override suspend fun setTheme(theme: AppTheme): Result<Unit> = save { preferences ->
        preferences[THEME] = theme.name
    }

    override fun getEnabledCategories(): Flow<Set<NotificationCategory>> {
        return context.preferencesDataStore.data.map { preferences ->
            NotificationCategory.entries.filter { preferences[keyOf(it)] ?: true }.toSet()
        }
    }

    override suspend fun setCategoryEnabled(
        category: NotificationCategory,
        enabled: Boolean
    ): Result<Unit> = save { preferences ->
        preferences[keyOf(category)] = enabled
    }

    // Without a choice in the app, the language of the phone is used
    override fun getLanguage(): AppLanguage {
        val chosen = AppCompatDelegate.getApplicationLocales()
        val language = if (chosen.isEmpty) Locale.getDefault().language else chosen[0]?.language
        return if (language == "es") AppLanguage.SPANISH else AppLanguage.ENGLISH
    }

    // AppCompat stores the choice and recreates the screen with the new texts
    override fun setLanguage(language: AppLanguage) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.tag))
    }

    private fun keyOf(category: NotificationCategory) =
        booleanPreferencesKey("notification_${category.name.lowercase()}")

    private suspend fun save(change: (MutablePreferences) -> Unit): Result<Unit> {
        return try {
            context.preferencesDataStore.edit { preferences -> change(preferences) }
            Result.success(Unit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}
