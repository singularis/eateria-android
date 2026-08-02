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
import com.singularis.eateria.util.Secrets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class TokenRequest(
    val provider: String,
    val idToken: String,
    val email: String,
    val name: String?,
    val profilePictureURL: String?,
    val previousAnonymousUuid: String? = null,
)

data class TokenResponse(
    val token: String,
    val expiresIn: Int,
    val userEmail: String,
    val userName: String?,
    val profilePictureURL: String?,
)

class AuthenticationService(
    private val context: Context,
) {
    private val baseUrl: String
        get() = AppEnvironment.getInstance().baseURL.trimEnd('/') + "/"

    private val client =
        OkHttpClient
            .Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

    // Modern Credential Manager instance
    private val credentialManager = CredentialManager.create(context)

    /**
     * Auth over OkHttp + org.json (no Gson/Retrofit reflection).
     * R8 must not be able to rename JSON keys for login.
     */
    private suspend fun authenticate(
        endpoint: String,
        request: TokenRequest,
    ): TokenResponse =
        withContext(Dispatchers.IO) {
            val bodyJson =
                JSONObject()
                    .put("provider", request.provider)
                    .put("idToken", request.idToken)
                    .put("email", request.email)
                    .put("name", request.name ?: JSONObject.NULL)
                    .put("profilePictureURL", request.profilePictureURL ?: JSONObject.NULL)
                    .apply {
                        if (!request.previousAnonymousUuid.isNullOrBlank()) {
                            put("previous_anonymous_uuid", request.previousAnonymousUuid)
                        }
                    }

            val httpRequest =
                Request
                    .Builder()
                    .url(baseUrl + endpoint.trimStart('/'))
                    .post(bodyJson.toString().toRequestBody(JSON_MEDIA_TYPE))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .build()

            client.newCall(httpRequest).execute().use { response ->
                val raw = response.body.string()
                if (!response.isSuccessful) {
                    throw IllegalStateException("Auth HTTP ${response.code}: ${raw.take(300)}")
                }
                parseTokenResponse(raw, fallbackEmail = request.email, fallbackName = request.name)
            }
        }

    private fun parseTokenResponse(
        raw: String,
        fallbackEmail: String,
        fallbackName: String?,
    ): TokenResponse {
        val json = JSONObject(raw)
        val token = json.optString("token", "").trim()
        if (token.isEmpty()) {
            throw IllegalStateException("Auth response missing token")
        }
        val userEmail =
            json.optString("userEmail", "")
                .ifBlank { json.optString("user_email", "") }
                .ifBlank { fallbackEmail }
                .trim()
        if (userEmail.isEmpty()) {
            throw IllegalStateException("Auth response missing userEmail")
        }
        val userName =
            json.optNonBlank("userName")
                ?: json.optNonBlank("user_name")
                ?: fallbackName
        val profilePictureURL =
            json.optNonBlank("profilePictureURL")
                ?: json.optNonBlank("profile_picture_url")
        val expiresIn = json.optInt("expiresIn", json.optInt("expires_in", 0))
        return TokenResponse(
            token = token,
            expiresIn = expiresIn,
            userEmail = userEmail,
            userName = userName,
            profilePictureURL = profilePictureURL,
        )
    }

    private fun JSONObject.optNonBlank(key: String): String? {
        if (!has(key) || isNull(key)) return null
        return optString(key).trim().takeIf { it.isNotEmpty() && it != "null" }
    }

    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
        private val USER_EMAIL = stringPreferencesKey("user_email")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val PROFILE_PICTURE_URL = stringPreferencesKey("profile_picture_url")
        private val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
        private val SOFT_LIMIT = stringPreferencesKey("soft_limit")
        private val HARD_LIMIT = stringPreferencesKey("hard_limit")
        private val CUSTOM_PROTEIN_GOAL = stringPreferencesKey("custom_protein_goal")
        private val CUSTOM_FAT_GOAL = stringPreferencesKey("custom_fat_goal")
        private val CUSTOM_CARBS_GOAL = stringPreferencesKey("custom_carbs_goal")
        private val HAS_USER_HEALTH_DATA = booleanPreferencesKey("has_user_health_data")
        private val DISPLAY_MODE_FULL = booleanPreferencesKey("display_mode_full")
        private val IS_ANONYMOUS = booleanPreferencesKey("is_anonymous")
        private val ANONYMOUS_UUID = stringPreferencesKey("anonymous_uuid")
    }

    private fun getSportCaloriesKey(dateKey: String): Preferences.Key<String> = stringPreferencesKey("sport_calories_$dateKey")

    // Flow for authentication state — email in DataStore is the UI gate; JWT must also exist.
    val isAuthenticated: Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            !preferences[USER_EMAIL].isNullOrEmpty() && !TokenStore.read(context).isNullOrEmpty()
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

    val isAnonymous: Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[IS_ANONYMOUS] ?: false
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
        val nickname = context.getSharedPreferences("eateria_prefs", android.content.Context.MODE_PRIVATE)
            .getString("user_nickname", "")
            ?.ifEmpty { null }
        val displayName =
            AnonymousUserIdentity.menuDisplayName(
                context = context,
                nickname = nickname,
                userName = name,
            )

        return when {
            AnonymousUserIdentity.isAnonymous(email = email, nickname = nickname, name = name) ->
                Localization.tr(context, "common.hello", "Hello") + " " +
                    AnonymousUserIdentity.defaultDisplayName(context)
            displayName.isNotEmpty() ->
                Localization.tr(context, "common.hello", "Hello") + " " + displayName
            !email.isNullOrEmpty() && !AnonymousUserIdentity.isAnonymousEmail(email) &&
                !AnonymousUserIdentity.isPrivateRelayEmail(email) -> {
                val firstName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
                Localization.tr(context, "common.hello", "Hello") + " " + firstName
            }
            else -> Localization.tr(context, "common.hello", "Hello")
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

    suspend fun signInAnonymously(): Boolean {
        return try {
            val uuid = java.util.UUID.randomUUID().toString()
            val fakeEmail = "anon_$uuid@anonymous.local"
            
            val tokenRequest = TokenRequest(
                provider = "anonymous",
                idToken = uuid,
                email = fakeEmail,
                name = "Guest",
                profilePictureURL = null
            )

            val response = authenticate("anonymous_auth", tokenRequest)
            if (response.token.isBlank()) return false

            TokenStore.save(context, response.token)

            context.dataStore.edit { preferences ->
                preferences[USER_EMAIL] = response.userEmail.ifBlank { fakeEmail }
                preferences[USER_NAME] = response.userName ?: "Guest"
                if (response.profilePictureURL != null) {
                    preferences[PROFILE_PICTURE_URL] = response.profilePictureURL
                }
                preferences[IS_ANONYMOUS] = true
                preferences[ANONYMOUS_UUID] = uuid
            }
            true
        } catch (e: Exception) {
            Log.e("AuthenticationService", "Anonymous sign-in failed", e)
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
        } catch (e: Exception) {
            Log.e("AuthenticationService", "Alternative sign-in also failed", e)
            false
        }
    }

    private suspend fun handleSignInResult(result: GetCredentialResponse): Boolean {
        when (val credential = result.credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    return try {
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential
                                .createFrom(credential.data)
                        handleGoogleIdToken(googleIdTokenCredential)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e("AuthenticationService", "Received an invalid google id token response", e)
                        false
                    }
                } else {
                    Log.e("AuthenticationService", "Unexpected type of credential")
                    return false
                }
            }
            is PasswordCredential -> {
                Log.d("AuthenticationService", "Password credential received")
                return false
            }
            is PublicKeyCredential -> {
                Log.d("AuthenticationService", "Passkey credential received")
                return false
            }
            else -> {
                Log.e("AuthenticationService", "Unexpected type of credential")
                return false
            }
        }
    }

    private suspend fun handleGoogleIdToken(googleIdTokenCredential: GoogleIdTokenCredential): Boolean {
        val idToken = googleIdTokenCredential.idToken
        val email = googleIdTokenCredential.id
        val name = googleIdTokenCredential.displayName
        val profilePictureURL = googleIdTokenCredential.profilePictureUri?.toString()

        val preferences = context.dataStore.data.first()
        val isAnon = preferences[IS_ANONYMOUS] == true
        val prevUuid = if (isAnon) preferences[ANONYMOUS_UUID] else null

        val tokenRequest =
            TokenRequest(
                provider = "google",
                idToken = idToken,
                email = email,
                name = name,
                profilePictureURL = profilePictureURL,
                previousAnonymousUuid = prevUuid,
            )

        return try {
            val tokenResponse = authenticate("eater_auth", tokenRequest)
            updateAuthenticationState(tokenResponse, fallbackEmail = email, fallbackName = name)
            !TokenStore.read(context).isNullOrEmpty()
        } catch (e: Exception) {
            Log.e("AuthenticationService", "Authentication failed", e)
            false
        }
    }

    private suspend fun updateAuthenticationState(
        response: TokenResponse,
        fallbackEmail: String? = null,
        fallbackName: String? = null,
    ) {
        if (response.token.isBlank()) {
            throw IllegalStateException("Refusing to persist empty auth token")
        }
        TokenStore.save(context, response.token)

        val email = response.userEmail.ifBlank { fallbackEmail.orEmpty() }.trim()
        if (email.isEmpty()) {
            throw IllegalStateException("Refusing to persist auth without userEmail")
        }

        context.dataStore.edit { preferences ->
            preferences[USER_EMAIL] = email
            (response.userName ?: fallbackName)?.let { preferences[USER_NAME] = it }
            response.profilePictureURL?.let { preferences[PROFILE_PICTURE_URL] = it }
            preferences[IS_ANONYMOUS] = false
            preferences.remove(ANONYMOUS_UUID)
        }
        Log.e(
            "AuthenticationService",
            "Auth persisted: email=$email tokenLen=${response.token.length}",
        )
    }

    suspend fun signOut() {
        try {
            credentialManager.clearCredentialState(androidx.credentials.ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("AuthenticationService", "Failed to clear credential state", e)
        }
        TokenStore.clear(context)
        clearAllUserData()
    }

    /**
     * Ends the guest session so the login screen appears, but keeps the anonymous UUID
     * so a later Google sign-in still sends [TokenRequest.previousAnonymousUuid] and the
     * guest's food history is migrated instead of lost.
     */
    suspend fun signOutForAccountUpgrade() {
        try {
            credentialManager.clearCredentialState(androidx.credentials.ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("AuthenticationService", "Failed to clear credential state", e)
        }
        TokenStore.clear(context)
        val anonymousUuid = context.dataStore.data.first()[ANONYMOUS_UUID]
        context.dataStore.edit { preferences ->
            preferences.clear()
            if (anonymousUuid != null) {
                preferences[ANONYMOUS_UUID] = anonymousUuid
                preferences[IS_ANONYMOUS] = true
            }
        }
    }

    suspend fun clearAllUserData() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    suspend fun deleteAccountAndClearData() {
        try {
            credentialManager.clearCredentialState(androidx.credentials.ClearCredentialStateRequest())
        } catch (e: Exception) {
            Log.e("AuthenticationService", "Failed to clear credential state", e)
        }
        TokenStore.clear(context)
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

    suspend fun getCustomProteinGoal(): Double? =
        context.dataStore.data.first()[CUSTOM_PROTEIN_GOAL]?.toDoubleOrNull()

    suspend fun getCustomFatGoal(): Double? =
        context.dataStore.data.first()[CUSTOM_FAT_GOAL]?.toDoubleOrNull()

    suspend fun getCustomCarbsGoal(): Double? =
        context.dataStore.data.first()[CUSTOM_CARBS_GOAL]?.toDoubleOrNull()

    suspend fun setCustomMacroGoals(
        protein: Double?,
        fat: Double?,
        carbs: Double?,
    ) {
        context.dataStore.edit { preferences ->
            if (protein != null) preferences[CUSTOM_PROTEIN_GOAL] = protein.toString()
            else preferences.remove(CUSTOM_PROTEIN_GOAL)
            if (fat != null) preferences[CUSTOM_FAT_GOAL] = fat.toString()
            else preferences.remove(CUSTOM_FAT_GOAL)
            if (carbs != null) preferences[CUSTOM_CARBS_GOAL] = carbs.toString()
            else preferences.remove(CUSTOM_CARBS_GOAL)
        }
    }

    suspend fun clearCustomMacroGoals() {
        context.dataStore.edit { preferences ->
            preferences.remove(CUSTOM_PROTEIN_GOAL)
            preferences.remove(CUSTOM_FAT_GOAL)
            preferences.remove(CUSTOM_CARBS_GOAL)
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
