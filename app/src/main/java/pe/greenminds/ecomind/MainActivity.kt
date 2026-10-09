package pe.greenminds.ecomind

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import pe.greenminds.ecomind.main.MainShell
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val localNetworkPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            Toast.makeText(this, R.string.local_backend_permission_required, Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EcoMindTheme {
                MainShell()
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
}
