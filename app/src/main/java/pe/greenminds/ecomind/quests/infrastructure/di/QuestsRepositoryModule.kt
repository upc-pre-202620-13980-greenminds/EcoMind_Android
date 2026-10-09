package pe.greenminds.ecomind.quests.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.greenminds.ecomind.quests.domain.repositories.QuestRepository
import pe.greenminds.ecomind.quests.infrastructure.local.LocalQuestRepository

// The only place to change when the remote implementations exist
@Module
@InstallIn(SingletonComponent::class)
interface QuestsRepositoryModule {

    @Binds
    fun bindQuestRepository(impl: LocalQuestRepository): QuestRepository
}
