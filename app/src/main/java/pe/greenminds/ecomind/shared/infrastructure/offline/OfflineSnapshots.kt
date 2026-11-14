package pe.greenminds.ecomind.shared.infrastructure.offline

import android.content.Context
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.lang.reflect.Type
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import pe.greenminds.ecomind.iam.domain.model.Session
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import retrofit2.HttpException

class OfflineSnapshots @Inject constructor(
    @ApplicationContext context: Context,
    private val dao: OfflineDao,
    private val sessions: SessionRepository
) {
    private val gson = Gson()
    private val connectivity = Connectivity(context)

    suspend fun session(): Session? = sessions.getSession().first()

    suspend fun <T> read(session: Session, key: String, type: Type): T? =
        dao.snapshot(session.accountId, key)?.let { gson.fromJson<T>(it, type) }

    fun <T> observe(session: Session, key: String, type: Type) =
        dao.observeSnapshot(session.accountId, key).map { data -> data?.let { gson.fromJson<T>(it, type) } }

    suspend fun <T> fetch(key: String, type: Type, remote: suspend (Session) -> Result<T>): Result<T> {
        val session = session() ?: return Result.failure(IllegalStateException("No saved account"))
        val canFetch = connectivity.connected && !session.isExpired(System.currentTimeMillis())
        val result = if (canFetch) remote(session) else Result.failure(IOException("Connection or sign-in required"))
        if (session() != session) return Result.failure(IOException("Account changed"))
        return result.fold(
            onSuccess = {
                dao.saveSnapshot(OfflineSnapshot(session.accountId, key, gson.toJson(it)))
                Result.success(it)
            },
            onFailure = { error ->
                val cached = read<T>(session, key, type)
                if (cached != null && (error is IOException || error is HttpException && error.code() >= 500)) {
                    Result.success(cached)
                } else result
            }
        )
    }
}
