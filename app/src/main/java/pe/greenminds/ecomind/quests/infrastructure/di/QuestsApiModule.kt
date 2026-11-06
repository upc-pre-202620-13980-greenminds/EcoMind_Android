package pe.greenminds.ecomind.quests.infrastructure.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.greenminds.ecomind.quests.infrastructure.remote.CollaborativeQuestApi
import pe.greenminds.ecomind.quests.infrastructure.remote.QuestApi
import retrofit2.Retrofit
import javax.inject.Singleton
import pe.greenminds.ecomind.users.infrastructure.remote.PartyPeopleApi

@Module
@InstallIn(SingletonComponent::class)
object QuestsApiModule {
    @Provides
    @Singleton
    fun providePartyPeopleApi(retrofit: Retrofit): PartyPeopleApi = retrofit.create(PartyPeopleApi::class.java)

    @Provides
    @Singleton
    fun provideQuestApi(retrofit: Retrofit): QuestApi = retrofit.create(QuestApi::class.java)

    @Provides
    @Singleton
    fun provideCollaborativeQuestApi(retrofit: Retrofit):
            CollaborativeQuestApi = retrofit.create(CollaborativeQuestApi::class.java)
}
