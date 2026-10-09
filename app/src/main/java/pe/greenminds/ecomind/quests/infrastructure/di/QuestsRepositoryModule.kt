package pe.greenminds.ecomind.quests.infrastructure.di

import dagger.Binds
import dagger.Provides
import pe.greenminds.ecomind.BuildConfig
import pe.greenminds.ecomind.quests.domain.repositories.QuestExecutionRepository
import pe.greenminds.ecomind.quests.infrastructure.local.LocalQuestExecutionRepository
import pe.greenminds.ecomind.quests.infrastructure.remote.*
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.greenminds.ecomind.quests.domain.repositories.QuestProgressRepository
import pe.greenminds.ecomind.quests.domain.repositories.QuestRepository
import pe.greenminds.ecomind.quests.infrastructure.local.LocalQuestProgressRepository
import pe.greenminds.ecomind.quests.infrastructure.local.LocalQuestRepository

// The only place to change when the remote implementations exist
@Module
@InstallIn(SingletonComponent::class)
interface QuestsRepositoryModule {

    companion object {
        @Provides fun quests(local: LocalQuestRepository, remote: RemoteQuestRepository): QuestRepository =
            if (BuildConfig.REMOTE_BACKEND) remote else local
        @Provides fun execution(local: LocalQuestExecutionRepository, remote: RemoteQuestExecutionRepository): QuestExecutionRepository =
            if (BuildConfig.REMOTE_BACKEND) remote else local
        @Provides fun progress(local: LocalQuestProgressRepository, remote: RemoteQuestProgressRepository): QuestProgressRepository =
            if (BuildConfig.REMOTE_BACKEND) remote else local
    }
}
