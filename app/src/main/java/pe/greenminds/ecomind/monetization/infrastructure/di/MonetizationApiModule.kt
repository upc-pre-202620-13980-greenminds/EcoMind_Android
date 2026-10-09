package pe.greenminds.ecomind.monetization.infrastructure.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.greenminds.ecomind.monetization.infrastructure.remote.MonetizationApi
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MonetizationApiModule {
    @Provides
    @Singleton
    fun provideMonetizationApi(retrofit: Retrofit): MonetizationApi =
        retrofit.create(MonetizationApi::class.java)
}
