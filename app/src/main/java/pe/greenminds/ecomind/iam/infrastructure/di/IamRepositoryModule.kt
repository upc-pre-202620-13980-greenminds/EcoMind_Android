package pe.greenminds.ecomind.iam.infrastructure.di

import dagger.Binds
import dagger.Provides
import pe.greenminds.ecomind.BuildConfig
import pe.greenminds.ecomind.iam.infrastructure.remote.RemoteAuthRepository
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.greenminds.ecomind.iam.domain.repositories.AuthRepository
import pe.greenminds.ecomind.iam.domain.repositories.SessionRepository
import pe.greenminds.ecomind.iam.infrastructure.local.LocalAuthRepository
import pe.greenminds.ecomind.iam.infrastructure.local.SessionDataStore

@Module
@InstallIn(SingletonComponent::class)
interface IamRepositoryModule {

    @Binds
    fun bindSessionRepository(impl: SessionDataStore): SessionRepository

    companion object {
        @Provides
        fun auth(local: LocalAuthRepository, remote: RemoteAuthRepository): AuthRepository =
            if (BuildConfig.REMOTE_BACKEND) remote else local
    }
}
