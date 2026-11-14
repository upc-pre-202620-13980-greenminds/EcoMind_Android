package pe.greenminds.ecomind.shared.infrastructure.offline

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Entity(primaryKeys = ["accountId", "key"])
data class OfflineSnapshot(val accountId: Long, val key: String, val data: String)

@Entity(primaryKeys = ["accountId", "questId"])
data class OfflineQuestRecord(
    val accountId: Long,
    val questId: Long,
    val data: String,
    val status: String
)

@Dao
interface OfflineDao {
    @Query("SELECT data FROM OfflineSnapshot WHERE accountId = :accountId AND `key` = :key")
    suspend fun snapshot(accountId: Long, key: String): String?

    @Query("SELECT data FROM OfflineSnapshot WHERE accountId = :accountId AND `key` = :key")
    fun observeSnapshot(accountId: Long, key: String): Flow<String?>

    @Upsert
    suspend fun saveSnapshot(snapshot: OfflineSnapshot)

    @Query("SELECT * FROM OfflineQuestRecord WHERE accountId = :accountId AND questId = :questId")
    suspend fun quest(accountId: Long, questId: Long): OfflineQuestRecord?

    @Query("SELECT * FROM OfflineQuestRecord WHERE accountId = :accountId ORDER BY questId")
    fun observeQuests(accountId: Long): Flow<List<OfflineQuestRecord>>

    @Query("SELECT * FROM OfflineQuestRecord WHERE accountId = :accountId AND status IN ('EDITED', 'PENDING')")
    suspend fun pendingQuests(accountId: Long): List<OfflineQuestRecord>

    @Query("DELETE FROM OfflineQuestRecord WHERE accountId = :accountId AND questId = :questId")
    suspend fun deleteQuest(accountId: Long, questId: Long)

    @Upsert
    suspend fun saveQuest(quest: OfflineQuestRecord)
}

@Database(entities = [OfflineSnapshot::class, OfflineQuestRecord::class], version = 1, exportSchema = false)
abstract class OfflineDatabase : RoomDatabase() {
    abstract fun offlineDao(): OfflineDao
}

@Module
@InstallIn(SingletonComponent::class)
object OfflineModule {
    @Provides
    @Singleton
    fun provideOfflineDatabase(@ApplicationContext context: Context): OfflineDatabase =
        Room.databaseBuilder(
            context,
            OfflineDatabase::class.java,
            context.noBackupFilesDir.resolve("offline.db").absolutePath
        ).build()

    @Provides
    fun provideOfflineDao(database: OfflineDatabase): OfflineDao = database.offlineDao()
}
