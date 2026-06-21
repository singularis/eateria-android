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

    private val foodNameToKey: Map<String, String> = mapOf(
        "Apple" to "food.apple",
        "Banana" to "food.banana",
        "Orange" to "food.orange",
        "Bread" to "food.bread",
        "Chicken" to "food.chicken",
        "Egg" to "food.egg",
        "Eggs" to "food.eggs",
        "Milk" to "food.milk",
        "Rice" to "food.rice",
        "Salad" to "food.salad",
        "Tomato" to "food.tomato",
        "Cheese" to "food.cheese",
        "Fish" to "food.fish",
        "Meat" to "food.meat",
        "Potato" to "food.potato",
        "Potatoes" to "food.potatoes",
        "Pasta" to "food.pasta",
        "Yogurt" to "food.yogurt",
        "Coffee" to "food.coffee",
        "Tea" to "food.tea"
    )

    fun translateFoodName(context: Context, name: String): String {
        val t = name.trim()
        if (t.isEmpty()) return name
        val key = foodNameToKey[t]
        if (key != null) {
            return tr(context, key, t)
        }
        return name
    }

    fun tr(
        context: Context,
        key: String,
        defaultValue: String? = null,
    ): String {
        val code = LanguageService.getCurrentCode(context)
        val map = translations(context.assets, code)
        map[key]?.let { return it }
        val en = translations(context.assets, "en")
        en[key]?.let { return it }
        return defaultValue ?: key
    }

    private fun translations(
        assets: AssetManager,
        code: String,
    ): Map<String, String> {
        cache[code]?.let { return it }
        val map = loadTranslations(assets, code)
        cache[code] = map
        return map
    }

    private fun loadTranslations(
        assets: AssetManager,
        code: String,
    ): Map<String, String> {
        val candidates =
            listOf(
                "Localization/${code.lowercase(Locale.getDefault())}.json",
                "${code.lowercase(Locale.getDefault())}.json",
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
