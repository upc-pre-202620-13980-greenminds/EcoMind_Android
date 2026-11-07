package pe.greenminds.ecomind.quests.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.greenminds.ecomind.quests.domain.repositories.FamilyPlanRepository
import pe.greenminds.ecomind.quests.infrastructure.implementation.RemoteFamilyPlanRepository
import pe.greenminds.ecomind.quests.infrastructure.remote.FamilyPlanApi
import pe.greenminds.ecomind.users.domain.repositories.FamilyMembershipRepository
import pe.greenminds.ecomind.users.infrastructure.implementation.RemoteFamilyMembershipRepository
import pe.greenminds.ecomind.users.infrastructure.remote.FamilyMembershipApi
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FamilyPlansModule {
    @Binds
    abstract fun bindFamilyPlans(impl: RemoteFamilyPlanRepository): FamilyPlanRepository

    @Binds
    abstract fun bindMemberships(impl: RemoteFamilyMembershipRepository): FamilyMembershipRepository

    companion object {
        @Provides
        @Singleton
        fun provideFamilyPlanApi(retrofit: Retrofit): FamilyPlanApi = retrofit.create(FamilyPlanApi::class.java)

        @Provides
        @Singleton
        fun provideFamilyMembershipApi(retrofit: Retrofit): FamilyMembershipApi = retrofit.create(FamilyMembershipApi::class.java)
    }
}
