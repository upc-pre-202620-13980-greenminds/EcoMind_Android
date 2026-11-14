package pe.greenminds.ecomind.community.infrastructure.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

private val Context.communityPreferencesDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "community_preferences")

@Module
@InstallIn(SingletonComponent::class)
object CommunityPreferenceDataStoreModule {

    @Provides
    @Singleton
    @Named("community_preferences")
    fun provideCommunityPreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.communityPreferencesDataStore
}