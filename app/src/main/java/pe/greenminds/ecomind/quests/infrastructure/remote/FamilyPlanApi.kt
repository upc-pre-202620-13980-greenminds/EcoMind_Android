package pe.greenminds.ecomind.quests.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface FamilyPlanApi {
    @GET("family-plans")
    suspend fun getPlans(@Query("familyId") familyId: Long): Response<List<FamilyPlanDto>>

    @GET("family-plans/active")
    suspend fun getActivePlan(@Query("familyId") familyId: Long): Response<FamilyPlanDto>

    @GET("family-plans/{planId}")
    suspend fun getPlan(@Path("planId") planId: Long): Response<FamilyPlanDto>

    @POST("family-plans")
    suspend fun createDraft(@Body request: CreateFamilyPlanRequest): Response<FamilyPlanDto>

    @PUT("family-plans/{planId}")
    suspend fun updateDraft(@Path("planId") planId: Long, @Body request: UpdateFamilyPlanRequest): Response<FamilyPlanDto>

    @POST("family-plans/{planId}/activate")
    suspend fun activate(@Path("planId") planId: Long): Response<FamilyPlanDto>

    @POST("family-plans/{planId}/complete")
    suspend fun complete(@Path("planId") planId: Long): Response<FamilyPlanDto>

    @DELETE("family-plans/{planId}")
    suspend fun delete(@Path("planId") planId: Long): Response<FamilyPlanDto>
}
