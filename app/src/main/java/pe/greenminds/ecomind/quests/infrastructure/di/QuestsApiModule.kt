package pe.greenminds.ecomind.quests.infrastructure.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.greenminds.ecomind.quests.infrastructure.remote.QuestApi
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object QuestsApiModule {
    @Provides
    @Singleton
    fun provideQuestApi(retrofit: Retrofit): QuestApi = retrofit.create(QuestApi::class.java)
}
