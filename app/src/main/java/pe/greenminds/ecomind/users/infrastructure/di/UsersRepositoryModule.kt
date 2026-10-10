package pe.greenminds.ecomind.users.infrastructure.di

import dagger.Binds
import dagger.Provides
import pe.greenminds.ecomind.BuildConfig
import pe.greenminds.ecomind.users.infrastructure.remote.*
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.greenminds.ecomind.users.domain.repositories.FamilyRepository
import pe.greenminds.ecomind.users.domain.repositories.FriendRepository
import pe.greenminds.ecomind.users.domain.repositories.ProfileRepository
import pe.greenminds.ecomind.users.infrastructure.local.LocalFamilyRepository
import pe.greenminds.ecomind.users.infrastructure.local.LocalFriendRepository
import pe.greenminds.ecomind.users.infrastructure.local.LocalProfileRepository

// The only place to change when the remote implementations exist
@Module
@InstallIn(SingletonComponent::class)
interface UsersRepositoryModule {

    companion object {
        @Provides fun profile(local: LocalProfileRepository, remote: RemoteProfileRepository): ProfileRepository =
            if (BuildConfig.REMOTE_BACKEND) remote else local
        @Provides fun friends(local: LocalFriendRepository, remote: RemoteFriendRepository): FriendRepository =
            if (BuildConfig.REMOTE_BACKEND) remote else local
        @Provides fun family(local: LocalFamilyRepository, remote: RemoteFamilyRepository): FamilyRepository =
            if (BuildConfig.REMOTE_BACKEND) remote else local
    }
}
