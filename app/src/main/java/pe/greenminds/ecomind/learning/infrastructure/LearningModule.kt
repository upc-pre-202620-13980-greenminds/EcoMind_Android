package pe.greenminds.ecomind.learning.infrastructure

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.greenminds.ecomind.learning.domain.LearningRepository
import retrofit2.Retrofit
import javax.inject.Singleton

@Module @InstallIn(SingletonComponent::class)
abstract class LearningRepositoryModule {
    @Binds abstract fun bindLearningRepository(repository: RemoteLearningRepository): LearningRepository
}

@Module @InstallIn(SingletonComponent::class)
object LearningApiModule {
    @Provides @Singleton fun provideLearningApi(retrofit: Retrofit): LearningApi = retrofit.create(LearningApi::class.java)
}
