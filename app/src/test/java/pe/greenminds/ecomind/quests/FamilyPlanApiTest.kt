package pe.greenminds.ecomind.quests

import com.google.gson.JsonParser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.junit.Assert.*
import org.junit.Test
import pe.greenminds.ecomind.quests.domain.valueobject.*
import pe.greenminds.ecomind.quests.infrastructure.implementation.RemoteFamilyPlanRepository
import pe.greenminds.ecomind.quests.infrastructure.remote.FamilyPlanApi
import pe.greenminds.ecomind.quests.infrastructure.remote.QuestApi
import pe.greenminds.ecomind.shared.infrastructure.remote.ApiException
import pe.greenminds.ecomind.users.domain.model.FamilyRole
import pe.greenminds.ecomind.users.infrastructure.implementation.RemoteFamilyMembershipRepository
import pe.greenminds.ecomind.users.infrastructure.remote.FamilyMembershipApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class FamilyPlanApiTest {
    private val planJson = """{"id":9,"familyId":2,"ownerUserId":20,"status":"ACTIVE","progress":50.0,
        "items":[{"id":11,"questId":3,"collaborativeSessionId":45,"progress":50.0}]}"""

    private class Backend {
        var code = 200
        var body = "[]"
        val requests = mutableListOf<Request>()
        val bodies = mutableListOf<String>()
        val retrofit: Retrofit = Retrofit.Builder().baseUrl("https://ecomind.invalid/api/v1/")
            .client(OkHttpClient.Builder().addInterceptor { chain ->
                requests += chain.request()
                bodies += Buffer().also { chain.request().body?.writeTo(it) }.readUtf8()
                Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1)
                    .code(code).message("Test response")
                    .body(body.toResponseBody("application/json".toMediaType())).build()
            }.build())
            .addConverterFactory(GsonConverterFactory.create()).build()

        fun repository() = RemoteFamilyPlanRepository(
            retrofit.create(FamilyPlanApi::class.java), retrofit.create(QuestApi::class.java)
        )
    }

    @Test fun createsDraftWithUniqueItemsAndNoOwnerInThePayload() = runBlocking {
        val backend = Backend().apply { code = 201; body = planJson.replace("ACTIVE", "DRAFT") }
        val plan = backend.repository().createDraft(2, listOf(3, 3)).getOrThrow()
        val json = JsonParser.parseString(backend.bodies.single()).asJsonObject
        assertEquals("POST", backend.requests.single().method)
        assertEquals("/api/v1/family-plans", backend.requests.single().url.encodedPath)
        assertEquals(2L, json["familyId"].asLong)
        assertFalse(json.has("ownerUserId"))
        assertEquals(1, json["items"].asJsonArray.size())
        assertEquals(FamilyPlanStatus.DRAFT, plan.status)
        assertEquals(45L, plan.items.single().collaborativeSessionId)
    }

    @Test fun updatesAndTransitionsUseTheNewBackendRoutes() = runBlocking {
        val backend = Backend().apply { body = planJson }
        val repository = backend.repository()
        repository.updateDraft(9, emptyList()).getOrThrow()
        repository.activate(9).getOrThrow()
        repository.complete(9).getOrThrow()
        repository.delete(9).getOrThrow()
        assertEquals(listOf("PUT", "POST", "POST", "DELETE"), backend.requests.map { it.method })
        assertEquals(listOf("/api/v1/family-plans/9", "/api/v1/family-plans/9/activate",
            "/api/v1/family-plans/9/complete", "/api/v1/family-plans/9"), backend.requests.map { it.url.encodedPath })
        assertTrue(backend.requests.all { it.url.queryParameter("ownerUserId") == null })
        assertEquals(0, JsonParser.parseString(backend.bodies.first()).asJsonObject["items"].asJsonArray.size())
    }

    @Test fun activePlan404IsAbsenceButForbiddenAndDetail404AreFailures() = runBlocking {
        val backend = Backend().apply { code = 404; body = """{"code":"NOT_FOUND","message":"No plan"}""" }
        val repository = backend.repository()
        assertNull(repository.getActivePlan(2).getOrThrow())
        assertEquals("2", backend.requests.single().url.queryParameter("familyId"))
        assertTrue(repository.getPlan(9).isFailure)
        backend.code = 403
        backend.body = """{"code":"FAMILY_PLAN_FORBIDDEN","message":"Owner only"}"""
        val error = repository.getActivePlan(2).exceptionOrNull() as ApiException
        assertEquals("FAMILY_PLAN_FORBIDDEN", error.code)
        assertEquals("Owner only", error.message)
    }

    @Test fun searchesOnlyFamilyQuestsWithTheBackendThemeFilter() = runBlocking {
        val backend = Backend()
        backend.repository().searchQuests(" water ", QuestCategory.WATER, QuestTheme.CHECKBOX).getOrThrow()
        val url = backend.requests.single().url
        assertEquals("/api/v1/quests/search", url.encodedPath)
        assertEquals("water", url.queryParameter("title"))
        assertEquals("WATER", url.queryParameter("category"))
        assertEquals("FAMILY", url.queryParameter("questType"))
        assertEquals("CHECKBOX", url.queryParameter("type"))
    }

    @Test fun membershipUsesCamelCaseAndDistinguishesNoFamilyFromNetworkFailure() = runBlocking {
        val backend = Backend().apply {
            body = """[{"id":8,"familyId":2,"userId":20,"familyRole":"CHILD"}]"""
        }
        val repository = RemoteFamilyMembershipRepository(backend.retrofit.create(FamilyMembershipApi::class.java))
        val member = repository.getMembership(20).getOrThrow()!!
        assertEquals(2L, member.familyId)
        assertEquals(FamilyRole.CHILD, member.familyRole)
        assertEquals("20", backend.requests.single().url.queryParameter("user_id"))
        backend.body = "[]"
        assertNull(repository.getMembership(20).getOrThrow())
        backend.code = 500
        backend.body = "Internal error"
        assertTrue(repository.getMembership(20).isFailure)
    }

    @Test fun cancelledRequestsPropagate() {
        val api = object : FamilyMembershipApi {
            override suspend fun getMemberships(userId: Long): retrofit2.Response<List<pe.greenminds.ecomind.users.infrastructure.remote.FamilyMemberDto>> {
                throw CancellationException("Screen closed")
            }
        }
        try {
            runBlocking { RemoteFamilyMembershipRepository(api).getMembership(20) }
            fail("Expected coroutine cancellation")
        } catch (_: CancellationException) { }
    }
}
