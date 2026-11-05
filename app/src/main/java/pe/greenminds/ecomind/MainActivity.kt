package pe.greenminds.ecomind

import android.Manifest
import android.content.pm.PackageManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import pe.greenminds.ecomind.main.MainShell
import pe.greenminds.ecomind.iam.interfaces.navigation.PasswordResetLink
import pe.greenminds.ecomind.iam.interfaces.navigation.PasswordResetLinkViewModel
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val resetLink: PasswordResetLinkViewModel by viewModels()
    private val localNetworkPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(this, R.string.local_backend_permission_required, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val fromHistory = intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0
        if (savedInstanceState == null && !fromHistory) receiveResetLink(intent)
        enableEdgeToEdge()
        setContent {
            val passwordResetToken = resetLink.token.collectAsStateWithLifecycle().value
            EcoMindTheme {
                MainShell(
                    passwordResetToken = passwordResetToken,
                    onPasswordResetHandled = {
                        resetLink.clear()
                        intent.data = null
                    }
                )
            }
        }
        // Only the local development server needs this permission on Android 17+.
        if (BuildConfig.DEBUG && Uri.parse(BuildConfig.API_BASE_URL).host == "10.0.2.2"
            && Build.VERSION.SDK_INT >= 37 && savedInstanceState == null
            && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_LOCAL_NETWORK)
                != PackageManager.PERMISSION_GRANTED
        ) {
            localNetworkPermission.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        receiveResetLink(intent)
    }

    private fun receiveResetLink(incoming: Intent) {
        val token = PasswordResetLink.tokenFrom(incoming.dataString) ?: return
        resetLink.receive(token)
        setIntent(Intent(incoming).setData(null))
    }
}
