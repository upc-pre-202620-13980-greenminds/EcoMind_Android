package pe.greenminds.ecomind.community.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.greenminds.ecomind.community.domain.repositories.CommunityDirectoryRepository
import pe.greenminds.ecomind.community.infrastructure.implementation.RemoteCommunityDirectoryRepository

@Module
@InstallIn(SingletonComponent::class)
interface CommunityDirectoryRepositoryModule {

    @Binds
    fun bindCommunityDirectoryRepository(
        implementation: RemoteCommunityDirectoryRepository
    ): CommunityDirectoryRepository
}