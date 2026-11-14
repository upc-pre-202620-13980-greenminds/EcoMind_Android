package pe.greenminds.ecomind

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.quests.infrastructure.offline.OfflineQuestStore

@HiltAndroidApp
class EcoMindApplication : Application() {
    @Inject lateinit var sessions: SessionRepository
    @Inject lateinit var offlineQuests: OfflineQuestStore

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            sessions.getSession().filterNotNull().collect { offlineQuests.resume(it) }
        }
    }
}
