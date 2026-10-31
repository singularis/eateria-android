package com.singularis.eateria

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.map
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import com.singularis.eateria.ui.theme.LocalAppScale
import com.singularis.eateria.services.AppSettingsService
import com.singularis.eateria.services.AuthenticationService
import com.singularis.eateria.services.LanguageService
import com.singularis.eateria.ui.theme.EateriaTheme
import com.singularis.eateria.ui.views.LoginView
import com.singularis.eateria.ui.views.RootTabView
import com.singularis.eateria.viewmodels.AuthViewModel
import com.singularis.eateria.viewmodels.MainViewModel

class MainActivity : ComponentActivity() {
    private lateinit var authService: AuthenticationService

    override fun onCreate(savedInstanceState: Bundle?) {
        // Backward-compatible edge-to-edge (enforced on API 35+). Avoid coloring
        // status/nav bars — those APIs are deprecated and flagged by Play Console.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        authService = AuthenticationService(this)
        // Set process Locale to stored language at startup
        com.singularis.eateria.services.LanguageService
            .applyCurrentLocale(this)

        setContent {
            val settings = AppSettingsService.getInstance()
            val storedScale by settings.fontScaleFlow.collectAsState()
            val followsSystem by settings.followsSystemTextSizeFlow.collectAsState()
            val density = LocalDensity.current
            val configuration = LocalConfiguration.current
            val scale =
                AppSettingsService.effectiveScale(
                    stored = storedScale,
                    followsSystem = followsSystem,
                    systemFontScale = configuration.fontScale,
                    screenWidthDp = configuration.screenWidthDp,
                )
            EateriaTheme {
                CompositionLocalProvider(
                    LocalDensity provides Density(density.density * scale, 1f),
                    LocalAppScale provides scale,
                    // Keep touch targets at 48 physical dp whatever the interface scale.
                    LocalMinimumInteractiveComponentSize provides 48.dp / scale,
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        EateriaApp()
                    }
                }
            }
        }
    }

    @Composable
    fun EateriaApp() {
        val context = this@MainActivity
        val currentLanguage by LanguageService.languageFlow(context).collectAsState(initial = LanguageService.getCurrentCode(context))
        val authViewModel: AuthViewModel = viewModel { AuthViewModel(authService) }
        val isAuthenticated by authViewModel.isAuthenticated.collectAsState(initial = false)
        key(currentLanguage) {
            if (isAuthenticated) {
                // Keyed per account so a different sign-in never sees the previous user's meals or weight.
                val accountFlow = remember(authViewModel) { authViewModel.userEmail.map { it.orEmpty() } }
                val accountKey by accountFlow.collectAsState(initial = null)
                accountKey?.let { account ->
                    val mainViewModel: MainViewModel =
                        viewModel(key = "main-$account") { MainViewModel(this@MainActivity) }
                    RootTabView(
                        viewModel = mainViewModel,
                        authViewModel = authViewModel,
                    )
                }
            } else {
                LoginView(
                    authViewModel = authViewModel,
                    activity = this@MainActivity,
                )
            }
        }
    }
}
