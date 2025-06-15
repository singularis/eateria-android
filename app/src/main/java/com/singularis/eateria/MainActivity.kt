package com.singularis.eateria

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.singularis.eateria.services.AuthenticationService
import com.singularis.eateria.ui.theme.EateriaTheme
import com.singularis.eateria.ui.views.ContentView
import com.singularis.eateria.ui.views.LoginView
import com.singularis.eateria.viewmodels.AuthViewModel
import com.singularis.eateria.viewmodels.MainViewModel

class MainActivity : ComponentActivity() {
    
    private lateinit var authService: AuthenticationService
    
    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val authViewModel: AuthViewModel by lazy {
            AuthViewModel(authService)
        }
        authViewModel.handleSignInResult(result.data)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        authService = AuthenticationService(this)
        
        setContent {
            EateriaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EateriaApp()
                }
            }
        }
    }
    
    @Composable
    fun EateriaApp() {
        val authViewModel: AuthViewModel = viewModel { AuthViewModel(authService) }
        val isAuthenticated by authViewModel.isAuthenticated.collectAsState(initial = false)
        
        if (isAuthenticated) {
            val mainViewModel: MainViewModel = viewModel { MainViewModel(this@MainActivity) }
            ContentView(
                viewModel = mainViewModel,
                authViewModel = authViewModel
            )
        } else {
            LoginView(
                authViewModel = authViewModel,
                onSignInClick = {
                    val signInIntent = authService.getSignInIntent()
                    signInLauncher.launch(signInIntent)
                }
            )
        }
    }
} 