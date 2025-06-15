package com.singularis.eateria.services

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
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
import java.security.Key
import java.util.Base64
import java.util.Date
import javax.crypto.spec.SecretKeySpec

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class TokenRequest(
    val provider: String,
    @SerializedName("idToken") val idToken: String,
    val email: String,
    val name: String?,
    @SerializedName("profilePictureURL") val profilePictureURL: String?
)

data class TokenResponse(
    val token: String,
    @SerializedName("expiresIn") val expiresIn: Int,
    @SerializedName("userEmail") val userEmail: String,
    @SerializedName("userName") val userName: String?,
    @SerializedName("profilePictureURL") val profilePictureURL: String?
)

data class ErrorResponse(
    val error: String,
    val message: String?
)

interface AuthApi {
    @POST("eater_auth")
    suspend fun authenticate(@Body tokenRequest: TokenRequest): TokenResponse
}

class AuthenticationService(private val context: Context) {
    
    private val secretKey = "StingSecertGeneratorSalt"
    private val baseUrl = "https://chater.singularis.work/"
    
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("Content-Type", "application/json")
            chain.proceed(requestBuilder.build())
        }
        .build()
    
    private val authApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(AuthApi::class.java)
    
    private val gson = Gson()
    
    companion object {
        private val AUTH_TOKEN = stringPreferencesKey("auth_token")
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val PROFILE_PICTURE_URL = stringPreferencesKey("profile_picture_url")
        private val TOKEN_CREATED_TIMESTAMP = floatPreferencesKey("token_created_timestamp")
        private val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
        private val SOFT_LIMIT = stringPreferencesKey("soft_limit")
        private val HARD_LIMIT = stringPreferencesKey("hard_limit")
        private val HAS_USER_HEALTH_DATA = booleanPreferencesKey("has_user_health_data")
    }
    
    // TODO: Migrate from deprecated GoogleSignIn and GoogleSignInClient to the latest recommended authentication APIs
    // Google Sign-In client
    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("your-google-oauth-client-id") // Replace with actual client ID
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }
    
    // Flow for authentication state
    val isAuthenticated: Flow<Boolean> = context.dataStore.data.map { preferences ->
        !preferences[USER_EMAIL].isNullOrEmpty()
    }
    
    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL]
    }
    
    val userName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME]
    }
    
    val userProfilePictureURL: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PROFILE_PICTURE_URL]
    }
    
    val hasSeenOnboarding: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[HAS_SEEN_ONBOARDING] ?: false
    }
    
    suspend fun setHasSeenOnboarding(seen: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAS_SEEN_ONBOARDING] = seen
        }
    }
    
    suspend fun getAuthToken(): String? {
        return context.dataStore.data.first()[AUTH_TOKEN]
    }
    
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
    
    // Google Sign-In intent
    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }
    
    // Handle Google Sign-In result
    suspend fun handleSignInResult(data: Intent?): Boolean {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            handleAuthenticationSuccess("google", account)
            true
        } catch (e: ApiException) {
            Log.w("AuthenticationService", "signInResult:failed code=" + e.statusCode)
            false
        }
    }
    
    private suspend fun handleAuthenticationSuccess(provider: String, account: GoogleSignInAccount) {
        val idToken = account.idToken ?: return
        val email = account.email ?: return
        val name = account.displayName
        val profilePictureURL = account.photoUrl?.toString()
        
        val tokenRequest = TokenRequest(
            provider = provider,
            idToken = idToken,
            email = email,
            name = name,
            profilePictureURL = profilePictureURL
        )
        
        try {
            val tokenResponse = authApi.authenticate(tokenRequest)
            updateAuthenticationState(tokenResponse)
        } catch (e: Exception) {
            Log.e("AuthenticationService", "Authentication failed", e)
            throw e
        }
    }
    
    private suspend fun updateAuthenticationState(response: TokenResponse) {
        val currentTimestamp = System.currentTimeMillis() / 1000f
        
        context.dataStore.edit { preferences ->
            preferences[AUTH_TOKEN] = response.token
            preferences[USER_EMAIL] = response.userEmail
            preferences[TOKEN_CREATED_TIMESTAMP] = currentTimestamp
            
            response.userName?.let { preferences[USER_NAME] = it }
            response.profilePictureURL?.let { preferences[PROFILE_PICTURE_URL] = it }
        }
    }
    
    suspend fun signOut() {
        googleSignInClient.signOut()
        clearAllUserData()
    }
    
    suspend fun clearAllUserData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    suspend fun deleteAccountAndClearData() {
        googleSignInClient.signOut()
        clearAllUserData()
    }
    
    suspend fun isTokenValidForSecureOperations(): Boolean {
        val preferences = context.dataStore.data.first()
        val token = preferences[AUTH_TOKEN] ?: return false
        val tokenCreatedTimestamp = preferences[TOKEN_CREATED_TIMESTAMP] ?: 0f
        
        val currentTime = System.currentTimeMillis() / 1000f
        val tokenAge = currentTime - tokenCreatedTimestamp
        val oneHourInSeconds = 3600f
        
        val isTokenFresh = tokenCreatedTimestamp > 0 && tokenAge < oneHourInSeconds
        
        return if (isTokenFresh) {
            validateTokenStructure(token)
        } else {
            try {
                verifyHS256(token, secretKey)
                true
            } catch (e: Exception) {
                validateTokenStructure(token)
            }
        }
    }
    
    suspend fun requiresFreshAuthentication(): Boolean {
        return !isTokenValidForSecureOperations()
    }
    
    private fun validateTokenStructure(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) return false
            
            val payload = String(Base64.getUrlDecoder().decode(parts[1]))
            val claims = gson.fromJson(payload, Map::class.java)
            
            // Check if token is expired
            val exp = (claims["exp"] as? Double)?.toLong()
            if (exp != null) {
                val expDate = Date(exp * 1000)
                expDate.after(Date())
            } else {
                true
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun verifyHS256(token: String, secret: String): Claims {
        val key: Key = SecretKeySpec(secret.toByteArray(), SignatureAlgorithm.HS256.jcaName)
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
    }
    
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
    
    suspend fun hasUserHealthData(): Boolean {
        return context.dataStore.data.first()[HAS_USER_HEALTH_DATA] ?: false
    }
    
    suspend fun setHasUserHealthData(hasData: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[HAS_USER_HEALTH_DATA] = hasData
        }
    }
} 