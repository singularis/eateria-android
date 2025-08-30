package com.singularis.eateria.services

import android.content.Context
import android.content.res.AssetManager
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.ConcurrentHashMap

object QuotesService {
    private val cache: MutableMap<String, List<String>> = ConcurrentHashMap()

    fun getRandomQuote(context: Context): String {
        val code = LanguageService.getCurrentCode(context)
        val quotes = getQuotes(context.assets, code)
        return if (quotes.isNotEmpty()) {
            quotes.random()
        } else {
            val englishQuotes = getQuotes(context.assets, "en")
            if (englishQuotes.isNotEmpty()) {
                englishQuotes.random()
            } else {
                "Eat well, live well!"
            }
        }
    }

    private fun getQuotes(assets: AssetManager, code: String): List<String> {
        cache[code]?.let { return it }
        val quotes = loadQuotes(assets, code)
        cache[code] = quotes
        return quotes
    }

    private fun loadQuotes(assets: AssetManager, code: String): List<String> {
        val candidates = listOf(
            "Localization/quotes/${code.lowercase()}.txt",
            "quotes/${code.lowercase()}.txt"
        )
        
        for (path in candidates) {
            try {
                assets.open(path).use { input ->
                    val quotes = BufferedReader(InputStreamReader(input))
                        .readLines()
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    return quotes
                }
            } catch (_: Exception) {
                // Continue to next candidate
            }
        }
        return emptyList()
    }

    fun clearCache() {
        cache.clear()
    }
}
