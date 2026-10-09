package pe.greenminds.ecomind.gamification.infrastructure.local

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import pe.greenminds.ecomind.gamification.domain.repositories.ShareRequest
import pe.greenminds.ecomind.gamification.domain.repositories.ShareRequestStore
import pe.greenminds.ecomind.shared.infrastructure.ExperienceDataStore
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

// Persist before posting: process death or a lost response must not create another publication.
@Singleton
class AchievementShareStore @Inject constructor(private val store: ExperienceDataStore) : ShareRequestStore {
    private fun key(user: Long, award: String) = stringPreferencesKey("achievement_share_${user}_$award")
    override suspend fun read(user: Long, award: String): ShareRequest? = store.dataStore.data.first()[key(user, award)]?.let {
        val parts = it.split('|')
        ShareRequest(parts[0], award, parts[1].toLong())
    }
    override suspend fun getOrCreate(user: Long, award: String, community: Long): ShareRequest {
        var request: ShareRequest? = null
        store.dataStore.edit { prefs ->
            val existing = prefs[key(user, award)]
            request = if (existing == null) ShareRequest(UUID.randomUUID().toString(), award, community)
                else existing.split('|').let { ShareRequest(it[0], award, it[1].toLong()) }
            check(request!!.communityId == community) { "Retry the original community selection" }
            prefs[key(user, award)] = "${request!!.requestId}|${request!!.communityId}"
        }
        return checkNotNull(request)
    }
}
