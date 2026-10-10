package pe.greenminds.ecomind.gamification.infrastructure.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.greenminds.ecomind.gamification.infrastructure.remote.GamificationApi
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GamificationApiModule {
    @Provides
    @Singleton
    fun provideGamificationApi(retrofit: Retrofit): GamificationApi =
        retrofit.create(GamificationApi::class.java)
}
