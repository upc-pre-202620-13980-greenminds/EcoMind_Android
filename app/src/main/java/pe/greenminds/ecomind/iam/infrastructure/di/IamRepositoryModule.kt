package pe.greenminds.ecomind.iam.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.greenminds.ecomind.iam.domain.repositories.AuthRepository
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.iam.infrastructure.implementation.RemoteAuthRepository
import pe.greenminds.ecomind.iam.infrastructure.local.SessionDataStore

@Module
@InstallIn(SingletonComponent::class)
interface IamRepositoryModule {

    @Binds
    fun bindSessionRepository(impl: SessionDataStore): SessionRepository

    @Binds
    fun bindAuthRepository(impl: RemoteAuthRepository): AuthRepository
}
