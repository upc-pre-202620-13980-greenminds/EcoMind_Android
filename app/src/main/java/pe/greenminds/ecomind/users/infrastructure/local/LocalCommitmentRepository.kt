package pe.greenminds.ecomind.users.infrastructure.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import pe.greenminds.ecomind.users.domain.repositories.CommitmentRepository
import javax.inject.Inject

// Its own file, so closing the session does not delete the commitments
private val Context.commitmentDataStore: DataStore<Preferences> by preferencesDataStore(name = "commitments")

// The web services have no route for the personal commitment,
// so it is kept on the device until that route exists
class LocalCommitmentRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : CommitmentRepository {

    companion object {
        // Set to true and run again to see the error state of the commitment screen
        private const val SIMULATE_SAVE_FAILURE = false
    }

    override suspend fun getCommitment(userId: Long): Result<String?> {
        return try {
            val preferences = context.commitmentDataStore.data.first()
            Result.success(preferences[keyOf(userId)])
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    override suspend fun saveCommitment(userId: Long, text: String): Result<Unit> {
        return try {
            check(!SIMULATE_SAVE_FAILURE) { "Simulated save failure" }
            context.commitmentDataStore.edit { preferences ->
                preferences[keyOf(userId)] = text
            }
            Result.success(Unit)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    // One value per user, so two accounts on the same phone keep their own commitment
    private fun keyOf(userId: Long) = stringPreferencesKey("commitment_$userId")
}
