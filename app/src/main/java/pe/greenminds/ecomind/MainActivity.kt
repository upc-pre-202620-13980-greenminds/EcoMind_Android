package pe.greenminds.ecomind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import pe.greenminds.ecomind.main.MainShell
import pe.greenminds.ecomind.shared.interfaces.theme.EcoMindTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EcoMindTheme {
                MainShell()
            }
        }
    }
}
