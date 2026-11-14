package io.github.ryoaonetsuki.mira

import android.content.Intent
import android.os.Bundle
import androidx.compose.runtime.mutableStateOf
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import io.github.ryoaonetsuki.mira.data.preferences.UserPreferences
import io.github.ryoaonetsuki.mira.ui.navigation.MiraNavGraph
import io.github.ryoaonetsuki.mira.ui.theme.MiraTheme

class MainActivity : FragmentActivity() {

    private val pendingNavigationIntent = mutableStateOf<Intent?>(null)

    private fun normalizeIntent(intent: Intent): Intent? {
        if (intent.action == Intent.ACTION_SEND) {
            val text = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (text.isNullOrBlank()) return null
            return intentToChatDeepLink(intent)?.let {
                Intent(Intent.ACTION_VIEW).setData(android.net.Uri.parse(it))
            }
        }
        return intent
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingNavigationIntent.value = normalizeIntent(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val userPrefs = UserPreferences(applicationContext)

        pendingNavigationIntent.value = normalizeIntent(intent)

        setContent {
            val themeMode by userPrefs.themeModeFlow.collectAsState(initial = "System")

            MiraTheme(themeMode = themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        MiraNavGraph(
                            navigationIntent = pendingNavigationIntent.value,
                            onNavigationIntentHandled = {
                                pendingNavigationIntent.value = null
                            }
                        )
                    }
                }
            }
        }
    }
}
