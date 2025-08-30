package com.singularis.eateria.services

import android.content.Context
import android.content.res.AssetManager
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

object Localization {
    private val cache: MutableMap<String, Map<String, String>> = ConcurrentHashMap()

    fun tr(context: Context, key: String, defaultValue: String? = null): String {
        val code = LanguageService.getCurrentCode(context)
        val map = translations(context.assets, code)
        map[key]?.let { return it }
        val en = translations(context.assets, "en")
        en[key]?.let { return it }
        return defaultValue ?: key
    }

    private fun translations(assets: AssetManager, code: String): Map<String, String> {
        cache[code]?.let { return it }
        val map = loadTranslations(assets, code)
        cache[code] = map
        return map
    }

    private fun loadTranslations(assets: AssetManager, code: String): Map<String, String> {
        val candidates = listOf(
            "Localization/${code.lowercase(Locale.getDefault())}.json",
            "${code.lowercase(Locale.getDefault())}.json"
        )
        for (path in candidates) {
            try {
                assets.open(path).use { input ->
                    val text = BufferedReader(InputStreamReader(input)).readText()
                    val obj = JSONObject(text)
                    val result = mutableMapOf<String, String>()
                    val keys = obj.keys()
                    while (keys.hasNext()) {
                        val k = keys.next()
                        val v = obj.optString(k, "")
                        if (v.isNotEmpty()) result[k] = v
                    }
                    return result
                }
            } catch (_: Exception) {
            }
        }
        return emptyMap()
    }

    fun clearCache() {
        cache.clear()
    }
}


