package pe.greenminds.ecomind.shared.infrastructure.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import pe.greenminds.ecomind.BuildConfig
import pe.greenminds.ecomind.gamification.infrastructure.remote.GamificationApi
import pe.greenminds.ecomind.iam.infrastructure.remote.AuthenticationApi
import pe.greenminds.ecomind.users.infrastructure.remote.UsersApi
import pe.greenminds.ecomind.quests.infrastructure.remote.QuestApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides @Singleton fun client(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS).build()
    @Provides @Singleton fun retrofit(client: OkHttpClient): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL).client(client)
        .addConverterFactory(GsonConverterFactory.create()).build()
    @Provides fun gamification(retrofit: Retrofit): GamificationApi = retrofit.create(GamificationApi::class.java)
    @Provides fun users(retrofit: Retrofit): UsersApi = retrofit.create(UsersApi::class.java)
    @Provides fun quests(retrofit: Retrofit): QuestApi = retrofit.create(QuestApi::class.java)
    @Provides fun authentication(retrofit: Retrofit): AuthenticationApi = retrofit.create(AuthenticationApi::class.java)
}
