package pe.greenminds.ecomind.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import pe.greenminds.ecomind.notifications.domain.model.NotificationType
import pe.greenminds.ecomind.notifications.infrastructure.remote.NotificationDto
import pe.greenminds.ecomind.notifications.infrastructure.remote.toDomain
import pe.greenminds.ecomind.notifications.infrastructure.remote.toDomainSkippingInvalid
import pe.greenminds.ecomind.notifications.interfaces.NotificationDestination
import pe.greenminds.ecomind.notifications.interfaces.destinationFor
import pe.greenminds.ecomind.shared.domain.model.NotificationCategory

// The inbox must keep working when the web services send values this version does not know
class NotificationMappersTest {

    @Test
    fun aKnownTypeKeepsItsCategoryAndOpensItsScreen() {
        val notification = dto(id = 1, type = "ACHIEVEMENT_UNLOCKED").toDomain()

        assertEquals(NotificationType.ACHIEVEMENT_UNLOCKED, notification.type)
        assertEquals(NotificationCategory.ACHIEVEMENTS_AND_BADGES, notification.category)
        assertEquals(NotificationDestination.MEDALS, destinationFor(notification.type))
    }

    @Test
    fun anUnknownTypeIsReadAsUnknownAndOpensNothing() {
        val notification = dto(id = 1, type = "SOMETHING_NEW").toDomain()

        assertEquals(NotificationType.UNKNOWN, notification.type)
        assertNull(destinationFor(notification.type))
    }

    @Test
    fun aRowWithAWrongDateIsLeftOutAndTheRestIsShown() {
        val inbox = listOf(
            dto(id = 1, type = "LEARNING_REMINDER"),
            dto(id = 2, type = "WEEKLY_SUMMARY", createdAt = "not a date"),
            dto(id = 3, type = "SOMETHING_NEW")
        ).toDomainSkippingInvalid()

        assertEquals(listOf(1L, 3L), inbox.map { it.id })
    }

    private fun dto(
        id: Long,
        type: String,
        createdAt: String = "2026-11-16T13:00:00Z"
    ) = NotificationDto(
        id = id,
        category = "ACHIEVEMENTS_AND_BADGES",
        type = type,
        title = "Achievement unlocked",
        message = "You earned the achievement: First quest.",
        read = false,
        createdAt = createdAt
    )
}
