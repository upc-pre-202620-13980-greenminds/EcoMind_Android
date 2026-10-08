package pe.greenminds.ecomind.community.infrastructure.local

import kotlinx.coroutines.delay
import pe.greenminds.ecomind.community.domain.model.CommunityEvent
import pe.greenminds.ecomind.community.domain.repositories.CommunityRepository
import pe.greenminds.ecomind.community.infrastructure.remote.CommunityEventDto
import pe.greenminds.ecomind.community.infrastructure.remote.toDomain
import javax.inject.Inject

// Demo implementation used until the web services are deployed.
// The data is fixed: the three events of the design.
class LocalCommunityRepository @Inject constructor() : CommunityRepository {

    companion object {
        private const val SIMULATED_DELAY_MILLIS = 400L
    }

    private val events = listOf(
        CommunityEventDto(
            id = 1L,
            title = "Communal harvest",
            description = "We will meet at the neighborhood organic garden to harvest " +
                "fresh produce on Sunday, November 9.",
            date = "2026-11-09"
        ),
        CommunityEventDto(
            id = 2L,
            title = "Beach cleanup",
            description = "We will meet at San Miguel Beach to remove plastics and waste " +
                "on Saturday, November 2.",
            date = "2026-11-02"
        ),
        CommunityEventDto(
            id = 3L,
            title = "Garbage collection",
            description = "We will meet at the central park to collect trash and clean up " +
                "the green areas on Saturday, October 26.",
            date = "2026-10-26"
        )
    )

    // Equivalent to GET /community/events
    override suspend fun getEvents(): Result<List<CommunityEvent>> {
        // Simulates the time a request to the web services would take
        delay(SIMULATED_DELAY_MILLIS)
        // Same mapper the remote implementation will use
        return Result.success(events.map { it.toDomain() })
    }
}
