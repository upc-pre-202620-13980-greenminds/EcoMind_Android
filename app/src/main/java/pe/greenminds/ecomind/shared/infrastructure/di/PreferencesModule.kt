package pe.greenminds.ecomind.shared.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.greenminds.ecomind.shared.domain.repositories.PreferencesRepository
import pe.greenminds.ecomind.shared.infrastructure.local.LocalPreferencesRepository

@Module
@InstallIn(SingletonComponent::class)
interface PreferencesModule {

    // Local until the web services have a route for preferences
    @Binds
    fun bindPreferencesRepository(impl: LocalPreferencesRepository): PreferencesRepository
}
