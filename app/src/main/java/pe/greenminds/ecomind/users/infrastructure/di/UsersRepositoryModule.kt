package pe.greenminds.ecomind.users.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.greenminds.ecomind.users.domain.repositories.ProfileRepository
import pe.greenminds.ecomind.users.infrastructure.local.LocalProfileRepository

@Module
@InstallIn(SingletonComponent::class)
interface UsersRepositoryModule {

    // Replace LocalProfileRepository here when the remote implementation exists
    @Binds
    fun bindProfileRepository(impl: LocalProfileRepository): ProfileRepository
}
