package pe.greenminds.ecomind.community.infrastructure.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import pe.greenminds.ecomind.community.infrastructure.remote.CommunityApi
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object CommunityApiModule {

    @Provides
    @Singleton
    fun provideCommunityApi(retrofit: Retrofit): CommunityApi =
        retrofit.create(CommunityApi::class.java)
}