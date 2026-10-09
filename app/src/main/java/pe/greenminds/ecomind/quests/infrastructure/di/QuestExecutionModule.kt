package pe.greenminds.ecomind.quests.infrastructure.di

import dagger.*
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit
import pe.greenminds.ecomind.quests.infrastructure.remote.QuestExecutionApi
import pe.greenminds.ecomind.quests.infrastructure.implementation.RemoteQuestExecutionRepository
import pe.greenminds.ecomind.quests.domain.repositories.QuestExecutionRepository

@Module
@InstallIn(SingletonComponent::class)
object QuestExecutionModule {
    @Provides @Singleton fun api(retrofit: Retrofit): QuestExecutionApi = retrofit.create(QuestExecutionApi::class.java)
    @Provides @Singleton fun repository(remote: RemoteQuestExecutionRepository): QuestExecutionRepository = remote
}
