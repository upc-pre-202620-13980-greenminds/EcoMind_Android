package pe.greenminds.ecomind.iam

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.view.KeyCharacterMap
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import java.util.ArrayDeque
import org.json.JSONObject
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/** Drives the installed Flutter app through its public Android accessibility tree. */
class FlutterControlledResetUiAutomationTest {
    @Test
    fun signedOutBackAndRelaunch() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        assumeTrue(
            InstrumentationRegistry.getArguments().getString("iamFlutterSignedOutOnly") == "true"
        )
        val context = instrumentation.targetContext
        val automation = instrumentation.uiAutomation
        if (!hasSignInFields(automation)) {
            launchFlutterMain(context)
        }
        waitForSignInFields(automation)
        assertSignInFieldsAreEmpty(automation)
        captureState(context.getExternalFilesDir(null), "signed-out-before-back")
        assertTrue(
            "SDK back navigation was unavailable",
            automation.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
        )
        SystemClock.sleep(POLL_MS)
        launchFlutterMain(context)
        waitForSignInFields(automation)
        assertSignInFieldsAreEmpty(automation)
        captureState(context.getExternalFilesDir(null), "relaunch-signed-out")
    }

    @Test
    fun controlledResetThenSignInAndSignOut() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        assumeTrue(
            InstrumentationRegistry.getArguments().getString("iamFlutterControlledUiTest") == "true"
        )

        val context = instrumentation.targetContext
        val fixtureFile = File(requireNotNull(context.getExternalFilesDir(null)), FIXTURE_NAME)
        assertTrue("Private Flutter reset fixture is missing", fixtureFile.isFile)
        val fixture = JSONObject(fixtureFile.readText())
        val link = fixture.getString("recoveryLink")
        val email = fixture.getString("email")
        val newPassword = fixture.getString("newPassword")
        val automation = instrumentation.uiAutomation
        val continueAfterReset = InstrumentationRegistry.getArguments()
            .getString("iamFlutterContinueAfterReset") == "true"
        if (continueAfterReset) {
            waitForAnyLabel(automation, listOf(PASSWORD_UPDATED, SIGN_IN, PROFILE_TITLE), BACKEND_TIMEOUT_MS)
            if (hasLabel(automation, PASSWORD_UPDATED)) {
                captureState(context.getExternalFilesDir(null), "password-updated")
            }
        } else {
            val resetUri = Uri.parse(link)
            assertTrue("Fixture is not a password reset link", resetUri.scheme == "ecomind")
            assertTrue("Fixture is not a password reset link", resetUri.host == "authentication")
            assertTrue("Fixture is not a password reset link", resetUri.path == "/reset-password")
            assertTrue("Fixture reset token is missing", !resetUri.getQueryParameter("token").isNullOrBlank())

            val launch = Intent(Intent.ACTION_VIEW, resetUri)
                .addCategory(Intent.CATEGORY_BROWSABLE)
                .setPackage(FLUTTER_PACKAGE)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launch)

            waitForLabels(automation, RESET_TITLE, timeoutMs = SCREEN_TIMEOUT_MS)
            enterEditableField(automation, isPassword = true, fieldIndex = 0, value = newPassword)
            enterEditableField(automation, isPassword = true, fieldIndex = 1, value = newPassword)
            assertTrue(
                "Password fields did not accept the reset input",
                waitForPasswordTextLengths(automation, newPassword.length)
            )
            captureState(context.getExternalFilesDir(null), "passwords-entered")
            clickLabel(automation, SAVE_PASSWORD)

            waitForLabels(automation, PASSWORD_UPDATED, timeoutMs = BACKEND_TIMEOUT_MS)
            captureState(context.getExternalFilesDir(null), "password-updated")
            clickLabel(automation, SIGN_IN)
        }
        if (!hasLabel(automation, PROFILE_TITLE)) {
            if (hasLabel(automation, PASSWORD_UPDATED)) clickLabel(automation, SIGN_IN)
            waitForSignInFields(automation)
            enterEditableField(automation, isPassword = false, fieldIndex = 0, value = email)
            enterEditableField(automation, isPassword = true, fieldIndex = 0, value = newPassword)
            clickLabel(automation, SIGN_IN)
        }

        waitForAnyLabel(automation, listOf(PROFILE_NAV, PROFILE_TITLE, SIGN_OUT), BACKEND_TIMEOUT_MS)
        if (hasLabel(automation, PROFILE_NAV)) clickLabel(automation, PROFILE_NAV)
        waitForLabels(automation, PROFILE_TITLE, timeoutMs = SCREEN_TIMEOUT_MS)
        captureState(context.getExternalFilesDir(null), "profile")
        waitForLabelWithScroll(automation, SIGN_OUT)
        clickLabel(automation, SIGN_OUT)
        waitForSignInFields(automation)
        assertSignInFieldsAreEmpty(automation)
        captureState(context.getExternalFilesDir(null), "signed-out")
    }

    private fun waitForEditableFields(
        automation: android.app.UiAutomation,
        isPassword: Boolean,
        minimum: Int
    ): List<AccessibilityNodeInfo> {
        val deadline = SystemClock.uptimeMillis() + SCREEN_TIMEOUT_MS
        while (SystemClock.uptimeMillis() < deadline) {
            val fields = editableNodes(automation).filter { it.isPassword == isPassword }
            if (fields.size >= minimum) return fields
            SystemClock.sleep(POLL_MS)
        }
        assertTrue("Password fields did not become available", false)
        return emptyList()
    }

    private fun waitForPasswordTextLengths(
        automation: android.app.UiAutomation,
        expectedLength: Int
    ): Boolean {
        val deadline = SystemClock.uptimeMillis() + PASSWORD_INPUT_TIMEOUT_MS
        while (SystemClock.uptimeMillis() < deadline) {
            val fields = editableNodes(automation).filter { it.isPassword }
            if (fields.size >= 2 && fields.take(2).all { it.text?.length == expectedLength }) return true
            SystemClock.sleep(POLL_MS)
        }
        return false
    }

    private fun enterEditableField(
        automation: android.app.UiAutomation,
        isPassword: Boolean,
        fieldIndex: Int,
        value: String
    ) {
        val minimum = fieldIndex + 1
        val fields = waitForEditableFields(automation, isPassword, minimum)
        assertTrue(
            "Input could not be tapped",
            fields[fieldIndex].performAction(AccessibilityNodeInfo.ACTION_CLICK)
        )
        SystemClock.sleep(POLL_MS)

        var refreshedField = waitForEditableFields(automation, isPassword, minimum)[fieldIndex]
        if (!isPassword && refreshedField.text?.toString() == value) return
        if (!refreshedField.text.isNullOrEmpty()) {
            val clearArgs = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, "")
            }
            refreshedField.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, clearArgs)
            SystemClock.sleep(POLL_MS)
            refreshedField = waitForEditableFields(automation, isPassword, minimum)[fieldIndex]
        }
        val currentLength = refreshedField.text?.length ?: 0
        assertTrue("Input could not be cleared safely", currentLength == 0)
        val args = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, value)
        }
        refreshedField.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        SystemClock.sleep(POLL_MS)

        var actualLength = editableTextLengths(automation, isPassword).getOrNull(fieldIndex) ?: -1
        if (actualLength == value.length) return
        assertTrue(
            "Input contains unexpected text",
            actualLength == 0
        )

        val blankField = waitForEditableFields(automation, isPassword, minimum)[fieldIndex]
        assertTrue(
            "Blank input could not be tapped",
            blankField.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        )
        SystemClock.sleep(POLL_MS)
        val events = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD)
            .getEvents(value.toCharArray())
        assertNotNull("Keyboard events could not be created", events)
        for (event in requireNotNull(events)) {
            assertTrue("Keyboard event could not be injected", automation.injectInputEvent(event, true))
        }
        SystemClock.sleep(POLL_MS)
        actualLength = editableTextLengths(automation, isPassword).getOrNull(fieldIndex) ?: -1
        assertTrue("Input did not accept SDK keyboard text", actualLength == value.length)
    }

    private fun editableTextLengths(automation: android.app.UiAutomation, isPassword: Boolean): List<Int> =
        editableNodes(automation).filter { it.isPassword == isPassword }.map { it.text?.length ?: 0 }

    private fun waitForSignInFields(automation: android.app.UiAutomation) {
        val deadline = SystemClock.uptimeMillis() + BACKEND_TIMEOUT_MS
        while (SystemClock.uptimeMillis() < deadline) {
            val nodes = editableNodes(automation)
            if (nodes.any { !it.isPassword } && nodes.any { it.isPassword }) return
            SystemClock.sleep(POLL_MS)
        }
        assertTrue("Sign-in fields did not become available", false)
    }

    private fun hasSignInFields(automation: android.app.UiAutomation): Boolean {
        val nodes = editableNodes(automation)
        return nodes.any { !it.isPassword } && nodes.any { it.isPassword }
    }

    private fun launchFlutterMain(context: android.content.Context) {
        val launch = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setComponent(ComponentName(FLUTTER_PACKAGE, "$FLUTTER_PACKAGE.MainActivity"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(launch)
    }

    private fun waitForLabelWithScroll(automation: android.app.UiAutomation, label: String) {
        val deadline = SystemClock.uptimeMillis() + SCREEN_TIMEOUT_MS
        var scrolls = 0
        while (SystemClock.uptimeMillis() < deadline) {
            if (hasLabel(automation, label)) return
            if (scrolls >= MAX_SCROLLS) break
            val scrollable = scrollableNodes(automation).firstOrNull() ?: break
            if (!scrollable.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)) break
            scrolls++
            SystemClock.sleep(POLL_MS)
        }
        assertTrue("Expected control was not available", hasLabel(automation, label))
    }

    private fun scrollableNodes(automation: android.app.UiAutomation): List<AccessibilityNodeInfo> {
        val root = automation.rootInActiveWindow ?: return emptyList()
        return traverse(root).filter { it.isVisibleToUser && it.isScrollable }
    }

    private fun captureState(externalFilesDir: File?, name: String) {
        assertNotNull("Private screenshot directory is unavailable", externalFilesDir)
        val directory = File(requireNotNull(externalFilesDir), EVIDENCE_DIRECTORY)
        assertTrue("Private screenshot directory could not be created", directory.isDirectory || directory.mkdirs())
        val screenshot = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        File(directory, "$name.png").outputStream().use {
            screenshot.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it)
        }
        screenshot.recycle()
        SystemClock.sleep(CAPTURE_HOLD_MS)
    }

    private fun assertSignInFieldsAreEmpty(automation: android.app.UiAutomation) {
        val fields = editableNodes(automation)
        val emailField = fields.firstOrNull { !it.isPassword }
        val passwordField = fields.firstOrNull { it.isPassword }
        assertNotNull("Signed-out email input is unavailable", emailField)
        assertNotNull("Signed-out password input is unavailable", passwordField)
        assertTrue("Signed-out email input was not cleared", emailField?.text.isNullOrEmpty())
        assertTrue("Signed-out password input was not cleared", passwordField?.text.isNullOrEmpty())
    }

    private fun clickLabel(automation: android.app.UiAutomation, label: String) {
        val deadline = SystemClock.uptimeMillis() + SCREEN_TIMEOUT_MS
        while (SystemClock.uptimeMillis() < deadline) {
            val target = findLabel(automation, label)
            if (target != null && clickNodeOrParent(target)) return
            SystemClock.sleep(POLL_MS)
        }
        assertTrue("Expected control was not available", false)
    }

    private fun waitForLabels(
        automation: android.app.UiAutomation,
        vararg labels: String,
        timeoutMs: Long
    ) {
        waitForAnyLabel(automation, labels.toList(), timeoutMs)
    }

    private fun waitForAnyLabel(
        automation: android.app.UiAutomation,
        labels: List<String>,
        timeoutMs: Long
    ) {
        val deadline = SystemClock.uptimeMillis() + timeoutMs
        while (SystemClock.uptimeMillis() < deadline) {
            if (labels.any { findLabel(automation, it) != null }) return
            SystemClock.sleep(POLL_MS)
        }
        assertTrue("Expected screen was not shown", false)
    }

    private fun hasLabel(automation: android.app.UiAutomation, label: String): Boolean =
        findLabel(automation, label) != null

    private fun findLabel(
        automation: android.app.UiAutomation,
        label: String
    ): AccessibilityNodeInfo? {
        val root = automation.rootInActiveWindow ?: return null
        return traverse(root).firstOrNull { node ->
            node.isVisibleToUser && !isEditable(node) && nodeLabels(node).any { it.equals(label, ignoreCase = true) }
        }
    }

    private fun editableNodes(automation: android.app.UiAutomation): List<AccessibilityNodeInfo> {
        val root = automation.rootInActiveWindow ?: return emptyList()
        return traverse(root).filter { it.isVisibleToUser && isEditable(it) }
    }

    private fun traverse(root: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        val output = ArrayList<AccessibilityNodeInfo>()
        val pending = ArrayDeque<AccessibilityNodeInfo>()
        pending.add(root)
        while (pending.isNotEmpty() && output.size < MAX_NODES) {
            val current = pending.removeFirst()
            output.add(current)
            for (index in 0 until current.childCount) {
                current.getChild(index)?.let(pending::addLast)
            }
        }
        return output
    }

    private fun nodeLabels(node: AccessibilityNodeInfo): List<String> = buildList {
        node.contentDescription?.toString()?.let(::add)
        node.hintText?.toString()?.let(::add)
        if (!isEditable(node) && !node.isPassword) node.text?.toString()?.let(::add)
    }

    private fun isEditable(node: AccessibilityNodeInfo): Boolean =
        node.isEditable || node.className?.toString() == EDIT_TEXT_CLASS

    private fun clickNodeOrParent(node: AccessibilityNodeInfo): Boolean {
        var candidate: AccessibilityNodeInfo? = node
        repeat(MAX_CLICK_ANCESTORS) {
            if (candidate?.isClickable == true && candidate.performAction(AccessibilityNodeInfo.ACTION_CLICK)) {
                return true
            }
            candidate = candidate?.parent
        }
        return false
    }

    private companion object {
        const val FLUTTER_PACKAGE = "pe.greenminds.ecomind_parents"
        const val FIXTURE_NAME = "flutter-fixture.json"
        const val EDIT_TEXT_CLASS = "android.widget.EditText"
        const val RESET_TITLE = "Create a new password"
        const val SAVE_PASSWORD = "Save new password"
        const val PASSWORD_UPDATED = "Password updated"
        const val SIGN_IN = "Sign in"
        const val PROFILE_NAV = "Profile"
        const val PROFILE_TITLE = "Your profile"
        const val SIGN_OUT = "Sign out"
        const val EVIDENCE_DIRECTORY = "flutter-ui-evidence"
        const val POLL_MS = 250L
        const val CAPTURE_HOLD_MS = 1_000L
        const val PASSWORD_INPUT_TIMEOUT_MS = 5_000L
        const val SCREEN_TIMEOUT_MS = 30_000L
        const val BACKEND_TIMEOUT_MS = 120_000L
        const val MAX_NODES = 500
        const val MAX_CLICK_ANCESTORS = 5
        const val MAX_SCROLLS = 8
    }
}
