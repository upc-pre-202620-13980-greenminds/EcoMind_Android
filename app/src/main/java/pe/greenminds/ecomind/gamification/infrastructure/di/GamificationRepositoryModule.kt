package pe.greenminds.ecomind.gamification.infrastructure.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.greenminds.ecomind.BuildConfig
import pe.greenminds.ecomind.gamification.domain.repositories.*
import pe.greenminds.ecomind.gamification.infrastructure.local.*
import pe.greenminds.ecomind.gamification.infrastructure.remote.*

@Module
@InstallIn(SingletonComponent::class)
object GamificationRepositoryModule {
    @Provides fun shareStore(store: AchievementShareStore): ShareRequestStore = store
    @Provides fun ranking(local: LocalRankingRepository, remote: RemoteRankingRepository): RankingRepository =
        if (BuildConfig.REMOTE_BACKEND) remote else local
    @Provides fun achievements(local: LocalAchievementRepository, remote: RemoteAchievementRepository): AchievementRepository =
        if (BuildConfig.REMOTE_BACKEND) remote else local
    @Provides fun progress(local: LocalProgressRepository, remote: RemoteProgressRepository): ProgressRepository =
        if (BuildConfig.REMOTE_BACKEND) remote else local
}
