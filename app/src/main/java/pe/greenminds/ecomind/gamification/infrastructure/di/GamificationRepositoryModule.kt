package pe.greenminds.ecomind.gamification.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.greenminds.ecomind.gamification.domain.repositories.RankingRepository
import pe.greenminds.ecomind.gamification.infrastructure.local.LocalRankingRepository

// The only place to change when the remote implementation exists
@Module
@InstallIn(SingletonComponent::class)
interface GamificationRepositoryModule {

    @Binds
    fun bindRankingRepository(impl: LocalRankingRepository): RankingRepository
}
