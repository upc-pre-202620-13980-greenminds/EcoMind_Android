package pe.greenminds.ecomind.shared.infrastructure

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private val Context.experienceDataStore by preferencesDataStore(name = "experience_preferences")

@Singleton
class ExperienceDataStore @Inject constructor(@ApplicationContext context: Context) {
    val dataStore = context.experienceDataStore
}
