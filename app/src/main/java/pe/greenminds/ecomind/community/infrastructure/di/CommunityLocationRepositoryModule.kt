package pe.greenminds.ecomind.community.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.greenminds.ecomind.community.domain.repositories.CommunityLocationRepository
import pe.greenminds.ecomind.community.infrastructure.implementation.AndroidCommunityLocationRepository

@Module
@InstallIn(SingletonComponent::class)
interface CommunityLocationRepositoryModule {

    @Binds
    fun bindCommunityLocationRepository(
        implementation: AndroidCommunityLocationRepository
    ): CommunityLocationRepository
}