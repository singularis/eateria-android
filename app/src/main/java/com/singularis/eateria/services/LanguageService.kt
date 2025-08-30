package com.singularis.eateria.services

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.singularis.eateria.services.dataStore
import kotlinx.coroutines.flow.first
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

object LanguageService {
    private val LANGUAGE_CODE = stringPreferencesKey("app_language_code")
    private val DISPLAY_NAME = stringPreferencesKey("app_language_name")

    suspend fun setLanguage(context: Context, code: String, displayName: String? = null) {
        val normalized = normalize(code)
        context.dataStore.edit {
            it[LANGUAGE_CODE] = normalized
            it[DISPLAY_NAME] = displayName ?: nativeName(normalized)
        }
        val auth = AuthenticationService(context)
        val email = auth.getUserEmail()
        if (!email.isNullOrEmpty()) {
            runCatching { GRPCService(context).setLanguage(email, normalized) }
        }
    }

    fun getCurrentCode(context: Context): String {
        return runCatching { kotlinx.coroutines.runBlocking { context.dataStore.data.first()[LANGUAGE_CODE] } }
            .getOrNull() ?: deviceDefault()
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


