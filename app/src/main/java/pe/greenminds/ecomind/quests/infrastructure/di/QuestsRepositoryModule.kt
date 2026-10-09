package pe.greenminds.ecomind.quests.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.greenminds.ecomind.quests.domain.repositories.QuestRepository
import pe.greenminds.ecomind.quests.infrastructure.implementation.RemoteQuestRepository

@Module
@InstallIn(SingletonComponent::class)
interface QuestsRepositoryModule {

    @Binds
    fun bindQuestRepository(impl: RemoteQuestRepository): QuestRepository
}
