package com.singularis.eateria.viewmodels

import androidx.activity.ComponentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.singularis.eateria.services.AuthenticationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authService: AuthenticationService,
) : ViewModel() {
    val isAuthenticated: Flow<Boolean> = authService.isAuthenticated
    val userEmail: Flow<String?> = authService.userEmail
    val userName: Flow<String?> = authService.userName
    val userProfilePictureURL: Flow<String?> = authService.userProfilePictureURL
    val hasSeenOnboarding: Flow<Boolean> = authService.hasSeenOnboarding
    val isFullDisplayMode: Flow<Boolean> = authService.isFullDisplayMode

    suspend fun signInWithCredentialManager(activity: ComponentActivity): Boolean = authService.signInWithCredentialManager(activity)

    fun signOut() {
        viewModelScope.launch {
            authService.signOut()
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            authService.deleteAccountAndClearData()
        }
    }

    fun setHasSeenOnboarding(seen: Boolean) {
        viewModelScope.launch {
            authService.setHasSeenOnboarding(seen)
        }
    }

    fun setFullDisplayMode(isFull: Boolean) {
        viewModelScope.launch {
            authService.setFullDisplayMode(isFull)
        }
    }

    suspend fun getGreeting(): String = authService.getGreeting()

    suspend fun getSoftLimit(): Int = authService.getSoftLimit()

    suspend fun getHardLimit(): Int = authService.getHardLimit()

    fun setSoftLimit(limit: Int) {
        viewModelScope.launch {
            authService.setSoftLimit(limit)
        }
    }

    fun setHardLimit(limit: Int) {
        viewModelScope.launch {
            authService.setHardLimit(limit)
        }
    }

    suspend fun hasUserHealthData(): Boolean = authService.hasUserHealthData()

    fun setHasUserHealthData(hasData: Boolean) {
        viewModelScope.launch {
            authService.setHasUserHealthData(hasData)
        }
    }
}
