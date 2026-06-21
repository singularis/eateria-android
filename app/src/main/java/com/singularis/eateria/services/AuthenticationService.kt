package com.singularis.eateria.services

import android.accounts.AccountManager
import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.singularis.eateria.util.Secrets
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.Date
import java.util.concurrent.TimeUnit

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class TokenRequest(
    val provider: String,
    @SerializedName("idToken") val idToken: String,
    val email: String,
    val name: String?,
    @SerializedName("profilePictureURL") val profilePictureURL: String?,
)

data class TokenResponse(
    val token: String,
    @SerializedName("expiresIn") val expiresIn: Int,
    @SerializedName("userEmail") val userEmail: String,
    @SerializedName("userName") val userName: String?,
    @SerializedName("profilePictureURL") val profilePictureURL: String?,
)

data class ErrorResponse(
    val error: String,
    val message: String?,
)

interface AuthApi {
    @POST
    suspend fun authenticate(
        @retrofit2.http.Url url: String,
        @Body tokenRequest: TokenRequest,
    ): TokenResponse
}

class AuthenticationService(
    private val context: Context,
) {
    private val baseUrl: String
        get() = AppEnvironment.getInstance().baseURL + "/"

    private val client =
        OkHttpClient
            .Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder =
                    original
                        .newBuilder()
                        .header("Content-Type", "application/json")
                chain.proceed(requestBuilder.build())
            }.build()

    private val authApi =
        Retrofit
            .Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)

    private val gson = Gson()

    // Modern Credential Manager instance
    private val credentialManager = CredentialManager.create(context)

    companion object {
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val PROFILE_PICTURE_URL = stringPreferencesKey("profile_picture_url")
        private val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
        private val SOFT_LIMIT = stringPreferencesKey("soft_limit")
        private val HARD_LIMIT = stringPreferencesKey("hard_limit")
        private val HAS_USER_HEALTH_DATA = booleanPreferencesKey("has_user_health_data")
        private val DISPLAY_MODE_FULL = booleanPreferencesKey("display_mode_full")
    }

    private fun getSportCaloriesKey(dateKey: String): Preferences.Key<String> = stringPreferencesKey("sport_calories_$dateKey")

    // Flow for authentication state
    val isAuthenticated: Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            !preferences[USER_EMAIL].isNullOrEmpty()
        }

    val userEmail: Flow<String?> =
        context.dataStore.data.map { preferences ->
            preferences[USER_EMAIL]
        }

    val userName: Flow<String?> =
        context.dataStore.data.map { preferences ->
            preferences[USER_NAME]
        }

    val userProfilePictureURL: Flow<String?> =
        context.dataStore.data.map { preferences ->
            preferences[PROFILE_PICTURE_URL]
        }

    val hasSeenOnboarding: Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[HAS_SEEN_ONBOARDING] ?: false
        }

    val isFullDisplayMode: Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[DISPLAY_MODE_FULL] ?: false
        }

    suspend fun setHasSeenOnboarding(seen: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAS_SEEN_ONBOARDING] = seen
        }
    }

    suspend fun setFullDisplayMode(isFull: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DISPLAY_MODE_FULL] = isFull
        }
    }

    suspend fun getAuthToken(): String? = TokenStore.read(context)

    suspend fun getUserEmail(): String? = context.dataStore.data.first()[USER_EMAIL]

    suspend fun getGreeting(): String {
        val preferences = context.dataStore.data.first()
        val name = preferences[USER_NAME]
        val email = preferences[USER_EMAIL]

        return when {
            !name.isNullOrEmpty() -> "Hello $name"
            !email.isNullOrEmpty() -> {
                val firstName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
                "Hello $firstName"
            }
            else -> "Hello"
        }
    }

    // Modern Credential Manager sign-in method
    suspend fun signInWithCredentialManager(activity: ComponentActivity): Boolean {
        Log.e("AuthenticationService", "=== DEBUG: Starting Google Sign-In ===")
        Log.e("AuthenticationService", "Package: ${context.packageName}")

        return try {
            // Check if Google Play Services is available
            val googlePlayServicesAvailable = checkGooglePlayServicesAvailability()
            Log.e("AuthenticationService", "Google Play Services available: $googlePlayServicesAvailable")
            Log.e("AuthenticationService", "Using Server Client ID: ${Secrets.googleClientId}")

            val googleIdOption =
                GetGoogleIdOption
                    .Builder()
                    .setServerClientId(Secrets.googleClientId)
                    .setFilterByAuthorizedAccounts(false) // Allow all accounts, not just authorized ones
                    .setAutoSelectEnabled(false) // Don't auto-select, let user choose
                    .build()

            Log.e("AuthenticationService", "Created GoogleIdOption with filterByAuthorizedAccounts=false, autoSelectEnabled=false")

            val request =
                GetCredentialRequest
                    .Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

            Log.e("AuthenticationService", "Calling credentialManager.getCredential...")

            val result =
                credentialManager.getCredential(
                    context = activity,
                    request = request,
                )

            Log.e("AuthenticationService", "SUCCESS: Got credential result!")
            handleSignInResult(result)
            true
        } catch (e: GetCredentialException) {
            Log.e("AuthenticationService", "FAILED: Credential Manager sign in failed", e)
            Log.e("AuthenticationService", "Exception type: ${e.javaClass.simpleName}")
            Log.e("AuthenticationService", "Exception message: ${e.message}")
            Log.e("AuthenticationService", "Exception cause: ${e.cause}")

            // More specific error handling
            when (e) {
                is NoCredentialException -> {
                    Log.e("AuthenticationService", "NoCredentialException - Possible causes:")
                    Log.e("AuthenticationService", "1. No Google account signed in on device")
                    Log.e("AuthenticationService", "2. Google Play Services needs update")
                    Log.e("AuthenticationService", "3. SHA-1 fingerprint not configured in Google Console")
                    Log.e("AuthenticationService", "4. Client ID configuration mismatch")

                    // Try alternative approach
                    return tryAlternativeSignIn(activity)
                }
                else -> {
                    Log.e("AuthenticationService", "Other credential exception: ${e.javaClass.simpleName}")
                }
            }
            false
        }
    }

    private fun checkGooglePlayServicesAvailability(): Boolean =
        try {
            // Check if we can create credential manager successfully
            val testCredentialManager = CredentialManager.create(context)
            val hasGoogleAccounts = checkGoogleAccountsAvailable()
            Log.e("AuthenticationService", "Google accounts available: $hasGoogleAccounts")
            true
        } catch (e: Exception) {
            Log.e("AuthenticationService", "Google Play Services check failed", e)
            false
        }

    private fun checkGoogleAccountsAvailable(): Boolean =
        try {
            val accountManager = AccountManager.get(context)
            val accounts = accountManager.getAccountsByType("com.google")
            Log.e("AuthenticationService", "Found ${accounts.size} Google accounts on device")
            accounts.isNotEmpty()
        } catch (e: Exception) {
            Log.e("AuthenticationService", "Failed to check Google accounts", e)
            false
        }

    private suspend fun tryAlternativeSignIn(activity: ComponentActivity): Boolean {
        Log.e("AuthenticationService", "Attempting alternative sign-in approach...")
        return try {
            // Fallback to explicit Sign in with Google UI for users who haven't authorized the app
            val signInWithGoogleOption =
                com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
                    .Builder(Secrets.googleClientId)
                    .build()

            val request =
                GetCredentialRequest
                    .Builder()
                    .addCredentialOption(signInWithGoogleOption)
                    .build()

            val result =
                credentialManager.getCredential(
                    context = activity,
                    request = request,
                )

            Log.e("AuthenticationService", "SUCCESS: Alternative sign-in worked!")
            handleSignInResult(result)
            true
        } catch (e: Exception) {
            Log.e("AuthenticationService", "Alternative sign-in also failed", e)
            false
        }
    }

    private suspend fun handleSignInResult(result: GetCredentialResponse) {
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential
                                .createFrom(credential.data)
                        handleGoogleIdToken(googleIdTokenCredential)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e("AuthenticationService", "Received an invalid google id token response", e)
                    }
                } else {
                    Log.e("AuthenticationService", "Unexpected type of credential")
                }
            }
            is PasswordCredential -> {
                // Handle password credential if needed
                Log.d("AuthenticationService", "Password credential received")
            }
            is PublicKeyCredential -> {
                // Handle passkey credential if needed
                Log.d("AuthenticationService", "Passkey credential received")
            }
            else -> {
                Log.e("AuthenticationService", "Unexpected type of credential")
            }
        }
    }

    private suspend fun handleGoogleIdToken(googleIdTokenCredential: GoogleIdTokenCredential) {
        val idToken = googleIdTokenCredential.idToken
        val email = googleIdTokenCredential.id
        val name = googleIdTokenCredential.displayName
        val profilePictureURL = googleIdTokenCredential.profilePictureUri?.toString()

        val tokenRequest =
            TokenRequest(
                provider = "google",
                idToken = idToken,
                email = email,
                name = name,
                profilePictureURL = profilePictureURL,
            )

        try {
            val endpoint = "eater_auth"
            val tokenResponse = authApi.authenticate(endpoint, tokenRequest)
            updateAuthenticationState(tokenResponse)
        } catch (e: Exception) {
            Log.e("AuthenticationService", "Authentication failed", e)
            throw e
        }
    }

    private suspend fun updateAuthenticationState(response: TokenResponse) {
        val currentTimestamp = System.currentTimeMillis() / 1000f

        TokenStore.save(context, response.token)

        context.dataStore.edit { preferences ->
            preferences[USER_EMAIL] = response.userEmail

            response.userName?.let { preferences[USER_NAME] = it }
            response.profilePictureURL?.let { preferences[PROFILE_PICTURE_URL] = it }
        }
    }

    suspend fun signOut() {
        TokenStore.clear(context)
        clearAllUserData()
    }

    suspend fun clearAllUserData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun deleteAccountAndClearData() {
        clearAllUserData()
    }

    suspend fun isTokenValidForSecureOperations(): Boolean {
        // Tokens are valid for 3 years per new architecture. We can just check if it exists.
        return TokenStore.read(context) != null
    }

    suspend fun requiresFreshAuthentication(): Boolean = !isTokenValidForSecureOperations()

    // Calorie limits management
    suspend fun getSoftLimit(): Int {
        val preferences = context.dataStore.data.first()
        return preferences[SOFT_LIMIT]?.toIntOrNull() ?: 1900
    }

    suspend fun getHardLimit(): Int {
        val preferences = context.dataStore.data.first()
        return preferences[HARD_LIMIT]?.toIntOrNull() ?: 2100
    }

    suspend fun setSoftLimit(limit: Int) {
        context.dataStore.edit { preferences ->
            preferences[SOFT_LIMIT] = limit.toString()
        }
    }

    suspend fun setHardLimit(limit: Int) {
        context.dataStore.edit { preferences ->
            preferences[HARD_LIMIT] = limit.toString()
        }
    }

    suspend fun hasUserHealthData(): Boolean = context.dataStore.data.first()[HAS_USER_HEALTH_DATA] ?: false

    suspend fun setHasUserHealthData(hasData: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAS_USER_HEALTH_DATA] = hasData
        }
    }

    suspend fun getSportCalories(dateKey: String): Int {
        val preferences = context.dataStore.data.first()
        val sportCaloriesKey = getSportCaloriesKey(dateKey)
        return preferences[sportCaloriesKey]?.toIntOrNull() ?: 0
    }

    suspend fun setSportCalories(
        dateKey: String,
        calories: Int,
    ) {
        context.dataStore.edit { preferences ->
            val sportCaloriesKey = getSportCaloriesKey(dateKey)
            preferences[sportCaloriesKey] = calories.toString()
        }
    }
}
