package pe.greenminds.ecomind.quests.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.greenminds.ecomind.quests.domain.repositories.CollaborativeQuestRepository
import pe.greenminds.ecomind.quests.domain.repositories.QuestRepository
import pe.greenminds.ecomind.quests.infrastructure.implementation.RemoteCollaborativeQuestRepository
import pe.greenminds.ecomind.quests.infrastructure.implementation.RemoteQuestRepository
import pe.greenminds.ecomind.quests.domain.repositories.QuestPartyPeopleRepository
import pe.greenminds.ecomind.quests.infrastructure.implementation.RemoteQuestPartyPeopleRepository

@Module
@InstallIn(SingletonComponent::class)
interface QuestsRepositoryModule {
    @Binds
    fun bindPartyPeopleRepository(impl: RemoteQuestPartyPeopleRepository): QuestPartyPeopleRepository

    @Binds
    fun bindQuestRepository(impl: RemoteQuestRepository): QuestRepository

    @Binds
    fun bindCollaborativeQuestRepository(impl: RemoteCollaborativeQuestRepository): CollaborativeQuestRepository
}
