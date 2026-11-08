package pe.greenminds.ecomind.community.infrastructure.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.flow.first
import pe.greenminds.ecomind.community.domain.repositories.CommunityPreferenceRepository

class CommunityPreferenceDataStore @Inject constructor(
    @Named("community_preferences")
    private val dataStore: DataStore<Preferences>
) : CommunityPreferenceRepository {

    override suspend fun getPreferredLocality(): String? =
        dataStore.data.first()[PREFERRED_LOCALITY]

    override suspend fun savePreferredLocality(locality: String) {
        dataStore.edit { preferences ->
            preferences[PREFERRED_LOCALITY] = locality.trim()
        }
    }

    private companion object {
        val PREFERRED_LOCALITY = stringPreferencesKey("preferred_locality")
    }
}