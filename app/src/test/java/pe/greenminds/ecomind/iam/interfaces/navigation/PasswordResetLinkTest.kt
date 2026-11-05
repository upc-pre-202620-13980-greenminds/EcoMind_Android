package pe.greenminds.ecomind.iam.interfaces.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PasswordResetLinkTest {
    @Test
    fun `reads a recovery token from the registered link`() {
        assertEquals("valid-token", PasswordResetLink.tokenFrom(
            "${PasswordResetLink.BASE_URI}?token=valid-token"
        ))
    }

    @Test
    fun `rejects unrelated destinations and ambiguous tokens`() {
        listOf(
            null,
            "https://authentication/reset-password?token=valid-token",
            "ecomind://another-host/reset-password?token=valid-token",
            "ecomind://authentication/another-path?token=valid-token",
            "${PasswordResetLink.BASE_URI}?token=first&token=second",
            "${PasswordResetLink.BASE_URI}?token=",
            "${PasswordResetLink.BASE_URI}?token=%20",
            "${PasswordResetLink.BASE_URI}?token=%invalid",
            "${PasswordResetLink.BASE_URI}?token=valid-token#fragment"
        ).forEach { assertNull(PasswordResetLink.tokenFrom(it)) }
    }
}
