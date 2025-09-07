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
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.singularis.eateria.services.AuthenticationService
import com.singularis.eateria.services.LanguageService
import com.singularis.eateria.ui.theme.EateriaTheme
import com.singularis.eateria.ui.views.ContentView
import com.singularis.eateria.ui.views.LoginView
import com.singularis.eateria.viewmodels.AuthViewModel
import com.singularis.eateria.viewmodels.MainViewModel

class MainActivity : ComponentActivity() {
    private lateinit var authService: AuthenticationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        enableEdgeToEdge()

        // Ensure the app can draw behind system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        authService = AuthenticationService(this)
        // Set process Locale to stored language at startup
        com.singularis.eateria.services.LanguageService
            .applyCurrentLocale(this)

        setContent {
            EateriaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    EateriaApp()
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
                val mainViewModel: MainViewModel = viewModel { MainViewModel(this@MainActivity) }
                ContentView(
                    viewModel = mainViewModel,
                    authViewModel = authViewModel,
                )
            } else {
                LoginView(
                    authViewModel = authViewModel,
                    activity = this@MainActivity,
                )
            }
        }
    }
}
