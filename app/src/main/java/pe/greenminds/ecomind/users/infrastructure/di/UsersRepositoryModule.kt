package pe.greenminds.ecomind.users.infrastructure.di

import dagger.Binds
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

    @Binds
    fun bindProfileRepository(impl: LocalProfileRepository): ProfileRepository

    @Binds
    fun bindFriendRepository(impl: LocalFriendRepository): FriendRepository

    @Binds
    fun bindFamilyRepository(impl: LocalFamilyRepository): FamilyRepository
}
