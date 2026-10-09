package pe.greenminds.ecomind.monetization.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.greenminds.ecomind.monetization.domain.repositories.StoreRepository
import pe.greenminds.ecomind.monetization.infrastructure.local.LocalStoreRepository
import javax.inject.Singleton

// The only place to change when the remote implementation exists
@Module
@InstallIn(SingletonComponent::class)
interface MonetizationRepositoryModule {

    @Binds
    @Singleton
    fun bindStoreRepository(impl: LocalStoreRepository): StoreRepository
}
