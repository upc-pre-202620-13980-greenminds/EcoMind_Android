package pe.greenminds.ecomind.iam

import android.content.Intent
import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

/** Launches a private reset URI into the Flutter application without exposing it in runner args. */
class FlutterResetLinkDeliveryTest {
    @Test
    fun actionViewDeliversPrivateResetUriToFlutterPackage() {
        val arguments = InstrumentationRegistry.getArguments()
        assumeTrue(arguments.getString("iamFlutterDeepLinkTest") == "true")

        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val fixtureDirectory = requireNotNull(context.getExternalFilesDir(null))
        val fixture = File(fixtureDirectory, "reset-uri.txt")
        assertTrue("Private reset URI fixture is missing", fixture.isFile)

        val rawUri = fixture.readText().trim()
        assertTrue("Private reset URI fixture is empty", rawUri.isNotEmpty())
        val uri = Uri.parse(rawUri)
        assertTrue(
            "Reset URI does not match the configured password recovery route",
            uri.scheme == "ecomind"
                && uri.host == "authentication"
                && uri.path == "/reset-password"
                && !uri.getQueryParameter("token").isNullOrBlank()
        )

        val intent = Intent(Intent.ACTION_VIEW, uri)
            .addCategory(Intent.CATEGORY_BROWSABLE)
            .setPackage(FLUTTER_APPLICATION_ID)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        // The Flutter UI test independently verifies receipt. Do not print this URI or token.
        context.startActivity(intent)
    }

    private companion object {
        const val FLUTTER_APPLICATION_ID = "pe.greenminds.ecomind_parents"
    }
}
