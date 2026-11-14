package pe.greenminds.ecomind.iam.recovery

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import pe.greenminds.ecomind.R
import pe.greenminds.ecomind.iam.domain.model.EmailAddress
import pe.greenminds.ecomind.iam.interfaces.recovery.RequestPasswordRecoveryContent
import pe.greenminds.ecomind.iam.interfaces.recovery.RequestPasswordRecoveryUiState
import pe.greenminds.ecomind.iam.interfaces.recovery.ResetPasswordContent
import pe.greenminds.ecomind.iam.interfaces.recovery.ResetPasswordUiState
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme

class PasswordRecoveryScreenTest {
    @get:Rule val compose = createComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun requestedStateUsesGenericMessageAndOffersResendAndSignIn() {
        var requests = 0
        var backs = 0
        val email = "student@example.com"
        compose.setContent {
            EcoMindTheme {
                RequestPasswordRecoveryContent(
                    state = RequestPasswordRecoveryUiState(email = email, isRequested = true),
                    onEmailChange = {},
                    onRequestRecovery = { requests++ },
                    onBack = { backs++ }
                )
            }
        }
        compose.onNodeWithText(
            context.getString(R.string.recovery_check_email_description, EmailAddress.mask(email))
        ).assertIsDisplayed()
        compose.onAllNodes(hasSetTextAction()).assertCountEquals(0)
        compose.onNodeWithText(context.getString(R.string.recovery_resend_button)).performClick()
        compose.onNodeWithText(context.getString(R.string.recovery_back_to_sign_in)).performClick()
        compose.runOnIdle {
            assertEquals(1, requests)
            assertEquals(1, backs)
        }
    }

    @Test
    fun requestLoadingPreventsAnotherSubmit() {
        compose.setContent {
            EcoMindTheme {
                RequestPasswordRecoveryContent(
                    state = RequestPasswordRecoveryUiState(isLoading = true),
                    onEmailChange = {}, onRequestRecovery = {}, onBack = {}
                )
            }
        }
        compose.onNode(hasClickAction().and(hasAnyDescendant(
            hasContentDescription(context.getString(R.string.loading))
        )))
            .assertIsNotEnabled()
    }

    @Test
    fun invalidResetLinkOffersRecoveryWithoutPasswordFields() {
        var requests = 0
        compose.setContent {
            EcoMindTheme {
                ResetPasswordContent(
                    state = ResetPasswordUiState(hasInvalidLink = true),
                    onPasswordChange = {}, onConfirmPasswordChange = {}, onResetPassword = {},
                    onBack = { requests++ }, onCompleted = {}
                )
            }
        }
        compose.onNodeWithText(context.getString(R.string.recovery_invalid_link_title)).assertIsDisplayed()
        compose.onAllNodes(hasSetTextAction()).assertCountEquals(0)
        compose.onNodeWithText(context.getString(R.string.recovery_request_new_button)).performClick()
        compose.runOnIdle { assertEquals(1, requests) }
    }

    @Test
    fun completedResetWaitsForSignInAndKeepsCleanupRetryVisible() {
        var finishes = 0
        compose.setContent {
            EcoMindTheme {
                ResetPasswordContent(
                    state = ResetPasswordUiState(
                        isCompleted = true,
                        formError = R.string.recovery_session_clear_error
                    ),
                    onPasswordChange = {}, onConfirmPasswordChange = {}, onResetPassword = {},
                    onBack = {}, onCompleted = { finishes++ }
                )
            }
        }
        compose.onNodeWithText(context.getString(R.string.recovery_completed_title)).assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.recovery_session_clear_error)).assertIsDisplayed()
        compose.onAllNodes(hasSetTextAction()).assertCountEquals(0)
        compose.runOnIdle { assertEquals(0, finishes) }
        compose.onNodeWithText(context.getString(R.string.sign_in_button)).performClick()
        compose.runOnIdle { assertEquals(1, finishes) }
    }
}
