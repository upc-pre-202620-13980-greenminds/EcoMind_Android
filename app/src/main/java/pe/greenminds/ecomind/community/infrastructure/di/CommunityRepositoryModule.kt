package pe.greenminds.ecomind.community.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.greenminds.ecomind.community.domain.repositories.CommunityRepository
import pe.greenminds.ecomind.community.infrastructure.local.LocalCommunityRepository

@Module
@InstallIn(SingletonComponent::class)
interface CommunityRepositoryModule {

    @Binds
    fun bindCommunityRepository(impl: LocalCommunityRepository): CommunityRepository
}
