package pe.greenminds.ecomind.iam.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.greenminds.ecomind.iam.domain.repositories.AuthRepository
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.iam.infrastructure.implementation.RemoteAuthRepository
import pe.greenminds.ecomind.iam.infrastructure.local.SessionDataStore
import pe.greenminds.ecomind.iam.infrastructure.local.LocalAuthRepository
import pe.greenminds.ecomind.BuildConfig

@Module
@InstallIn(SingletonComponent::class)
interface IamRepositoryModule {

    @Binds
    fun bindSessionRepository(impl: SessionDataStore): SessionRepository

    companion object {
        @Provides
        fun provideAuthRepository(remote: RemoteAuthRepository, local: LocalAuthRepository): AuthRepository =
            if (BuildConfig.REMOTE_BACKEND) remote else local
    }
}
