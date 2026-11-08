package pe.greenminds.ecomind.community.infrastructure.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton
import pe.greenminds.ecomind.community.domain.repositories.CommunityPreferenceRepository
import pe.greenminds.ecomind.community.infrastructure.local.CommunityPreferenceDataStore

@Module
@InstallIn(SingletonComponent::class)
interface CommunityPreferenceRepositoryModule {
    @Binds
    fun bindCommunityPreferenceRepository(
        implementation: CommunityPreferenceDataStore
    ): CommunityPreferenceRepository
}