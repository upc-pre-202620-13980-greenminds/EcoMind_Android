package pe.greenminds.ecomind.gamification.infrastructure.di

import dagger.Module
import dagger.Binds
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.greenminds.ecomind.gamification.domain.repositories.*
import pe.greenminds.ecomind.gamification.infrastructure.local.*
import pe.greenminds.ecomind.gamification.infrastructure.remote.*

@Module
@InstallIn(SingletonComponent::class)
interface GamificationRepositoryModule {
    @Binds fun ranking(repository: RemoteRankingRepository): RankingRepository
    @Binds fun achievements(repository: RemoteAchievementRepository): AchievementRepository
    @Binds fun progress(repository: RemoteProgressRepository): ProgressRepository
    @Binds fun shares(store: AchievementShareStore): ShareRequestStore
}
