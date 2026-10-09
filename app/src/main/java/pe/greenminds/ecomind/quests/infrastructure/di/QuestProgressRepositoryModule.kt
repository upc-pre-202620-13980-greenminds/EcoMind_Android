package pe.greenminds.ecomind.quests.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.greenminds.ecomind.quests.domain.repositories.QuestProgressRepository
import pe.greenminds.ecomind.quests.infrastructure.implementation.LocalQuestProgressRepository

@Module
@InstallIn(SingletonComponent::class)
interface QuestProgressRepositoryModule {
    @Binds
    fun bindQuestProgressRepository(impl: LocalQuestProgressRepository): QuestProgressRepository
}
