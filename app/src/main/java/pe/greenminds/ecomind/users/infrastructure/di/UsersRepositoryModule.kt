package pe.greenminds.ecomind.users.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.greenminds.ecomind.users.domain.repositories.CommitmentRepository
import pe.greenminds.ecomind.users.domain.repositories.FamilyRepository
import pe.greenminds.ecomind.users.domain.repositories.FriendRepository
import pe.greenminds.ecomind.users.domain.repositories.ProfileRepository
import pe.greenminds.ecomind.users.infrastructure.local.LocalCommitmentRepository
import pe.greenminds.ecomind.users.infrastructure.implementation.RemoteFamilyRepository
import pe.greenminds.ecomind.users.infrastructure.implementation.RemoteFriendRepository
import pe.greenminds.ecomind.users.infrastructure.implementation.RemoteProfileRepository

// The only place to change when the remote implementations exist
@Module
@InstallIn(SingletonComponent::class)
interface UsersRepositoryModule {

    // Profile, friends and family are connected to the web services
    @Binds
    fun bindProfileRepository(impl: RemoteProfileRepository): ProfileRepository

    @Binds
    fun bindFriendRepository(impl: RemoteFriendRepository): FriendRepository

    @Binds
    fun bindFamilyRepository(impl: RemoteFamilyRepository): FamilyRepository

    // There is no route for the personal commitment, so it stays on the device
    @Binds
    fun bindCommitmentRepository(impl: LocalCommitmentRepository): CommitmentRepository
}
