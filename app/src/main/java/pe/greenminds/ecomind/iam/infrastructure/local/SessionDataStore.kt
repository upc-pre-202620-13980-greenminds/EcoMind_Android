package pe.greenminds.ecomind.iam.infrastructure.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pe.greenminds.ecomind.iam.domain.model.Session
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import javax.inject.Inject

class SessionDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SessionRepository {

    companion object {
        val ACCOUNT_ID = longPreferencesKey("account_id")
        val EMAIL = stringPreferencesKey("email")
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val EXPIRES_AT = longPreferencesKey("expires_at")
    }

    override fun getSession(): Flow<Session?> {
        return dataStore.data.map { preferences ->
            val accountId = preferences[ACCOUNT_ID]
            val email = preferences[EMAIL]
            val accessToken = preferences[ACCESS_TOKEN]
            val expiresAt = preferences[EXPIRES_AT]

            // The session only exists when every value was stored
            if (accountId == null || email == null || accessToken == null || expiresAt == null) {
                null
            } else {
                Session(accountId, email, accessToken, expiresAt)
            }
        }
    }

    override suspend fun saveSession(session: Session) {
        dataStore.edit { preferences ->
            preferences[ACCOUNT_ID] = session.accountId
            preferences[EMAIL] = session.email
            preferences[ACCESS_TOKEN] = session.accessToken
            preferences[EXPIRES_AT] = session.expiresAtMillis
        }
    }

    override suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
