package com.singularis.eateria.services

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.singularis.eateria.services.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

object LanguageService {
    private val LANGUAGE_CODE = stringPreferencesKey("app_language_code")
    private val DISPLAY_NAME = stringPreferencesKey("app_language_name")

    // Representative country per language code for sensible defaults and flag rendering
    private val representativeCountry: Map<String, String> = mapOf(
        "en" to "US", "es" to "ES", "fr" to "FR", "de" to "DE", "it" to "IT", "pt" to "PT",
        "ru" to "RU", "uk" to "UA", "zh" to "CN", "ja" to "JP", "ar" to "SA", "hi" to "IN",
        "bn" to "BD", "nl" to "NL", "sv" to "SE", "fi" to "FI", "da" to "DK", "no" to "NO",
        "tr" to "TR", "el" to "GR", "pl" to "PL", "cs" to "CZ", "sk" to "SK", "sl" to "SI",
        "hr" to "HR", "hu" to "HU", "lv" to "LV", "lt" to "LT", "et" to "EE", "ro" to "RO",
        "bg" to "BG", "ga" to "IE", "mt" to "MT", "th" to "TH", "ur" to "PK", "vi" to "VN",
        "be" to "BY", "ko" to "KR"
    )

    suspend fun setLanguage(context: Context, code: String, displayName: String? = null): Boolean {
        val normalized = normalize(code)
        val auth = AuthenticationService(context)
        val email = auth.getUserEmail()

        val backendOk = if (!email.isNullOrEmpty()) {
            runCatching { GRPCService(context).setLanguage(email, normalized) }.getOrDefault(false)
        } else true

        if (backendOk) {
            context.dataStore.edit {
                it[LANGUAGE_CODE] = normalized
                it[DISPLAY_NAME] = displayName ?: nativeName(normalized)
            }
            val country = representativeCountry[normalized]?.uppercase(Locale.getDefault())
                ?: normalized.uppercase(Locale.getDefault())
            Locale.setDefault(Locale(normalized, country))
            Localization.clearCache()
            QuotesService.clearCache()
            return true
        } else {
            context.dataStore.edit {
                it[LANGUAGE_CODE] = "en"
                it[DISPLAY_NAME] = nativeName("en")
            }
            val fallbackCountry = representativeCountry["en"] ?: "US"
            Locale.setDefault(Locale("en", fallbackCountry))
            Localization.clearCache()
            QuotesService.clearCache()
            return false
        }
    }

    fun getCurrentCode(context: Context): String {
        return runCatching { kotlinx.coroutines.runBlocking { context.dataStore.data.first()[LANGUAGE_CODE] } }
            .getOrNull() ?: deviceDefault()
    }

    fun languageFlow(context: Context): Flow<String> {
        return context.dataStore.data.map { prefs ->
            normalize(prefs[LANGUAGE_CODE] ?: deviceDefault())
        }
    }

    fun hasPersistedLanguage(context: Context): Boolean {
        return try {
            val prefs = kotlinx.coroutines.runBlocking { context.dataStore.data.first() }
            prefs[LANGUAGE_CODE] != null
        } catch (_: Exception) { false }
    }

    fun applyCurrentLocale(context: Context) {
        val code = getCurrentCode(context)
        val country = representativeCountry[code]?.uppercase(Locale.getDefault())
            ?: code.uppercase(Locale.getDefault())
        Locale.setDefault(Locale(code, country))
    }

    fun flagEmoji(code: String): String {
        val country = representativeCountry[normalize(code)] ?: return ""
        return country.uppercase(Locale.getDefault()).map { ch ->
            Character.toChars(0x1F1E6 - 'A'.code + ch.code).concatToString()
        }.joinToString("")
    }

    fun deviceDefault(): String {
        val device = Locale.getDefault().language
        return normalize(device)
    }

    fun normalize(code: String): String = code.lowercase(Locale.getDefault()).split("-").firstOrNull() ?: code.lowercase(Locale.getDefault())

    fun nativeName(code: String): String {
        val norm = normalize(code)
        return Locale(norm).getDisplayLanguage(Locale(norm)).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    fun availableLanguageCodes(context: Context): List<String> {
        return try {
            val dir = context.assets.list("Localization").orEmpty()
            dir.filter { it.endsWith(".json") }
                .map { it.removeSuffix(".json").lowercase(Locale.getDefault()) }
                .map { normalize(it) }
                .distinct()
                .sorted()
                .ifEmpty { fromLanguagesTxt(context) }
        } catch (_: Exception) {
            fromLanguagesTxt(context)
        }
    }

    private fun fromLanguagesTxt(context: Context): List<String> {
        return try {
            context.assets.open("languages.txt").use { input ->
                val names = BufferedReader(InputStreamReader(input)).readLines().map { it.trim() }.filter { it.isNotEmpty() }
                val preferred = Locale("en")
                names.mapNotNull { name ->
                    Locale.getAvailableLocales().map { it.language }.distinct().firstOrNull { code ->
                        preferred.getDisplayLanguage(Locale(code)).equals(name, ignoreCase = true)
                    }
                }.map { normalize(it) }.distinct().sorted()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}

// Removed reflection-based client; using GRPCService.setLanguage instead


