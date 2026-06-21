package com.singularis.eateria.services

import android.content.Context
import com.singularis.eateria.models.Product
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Persists per-dish "extras" added locally (lemon/wasabi/etc).
 * Backend currently supports only `addedSugarTsp`, so we keep these extras on-device.
 */
class FoodExtrasStore private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: FoodExtrasStore? = null

        fun getInstance(context: Context): FoodExtrasStore =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: FoodExtrasStore(context.applicationContext).also { INSTANCE = it }
            }

        private const val PREFS_NAME = "food_extras_store"
        private const val KEY_EXTRAS = "food_extras_by_time"

        const val SUGAR_KEY = "added_sugar_tsp"

        val definitions = mapOf(
            "lemon_5g" to Pair(5, 1),
            "honey_10g" to Pair(10, 30),
            "milk_50g" to Pair(50, 32),
            "soy_sauce_15g" to Pair(15, 10),
            "wasabi_3g" to Pair(3, 8),
            "spicy_pepper_5g" to Pair(5, 2)
        )
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun addSugar(time: Long, tsp: Int = 1) {
        if (tsp <= 0) return
        val store = load()
        val t = time.toString()
        val extras = store[t]?.toMutableMap() ?: mutableMapOf()
        extras[SUGAR_KEY] = (extras[SUGAR_KEY] ?: 0) + tsp
        store[t] = extras
        save(store)
    }

    fun addExtra(time: Long, extraKey: String) {
        val store = load()
        val t = time.toString()
        val extras = store[t]?.toMutableMap() ?: mutableMapOf()
        extras[extraKey] = (extras[extraKey] ?: 0) + 1
        store[t] = extras
        save(store)
    }

    fun extras(time: Long): Map<String, Int> {
        return load()[time.toString()] ?: emptyMap()
    }

    fun apply(products: List<Product>): List<Product> {
        return products.map { p ->
            val ex = extras(p.time).toMutableMap()
            if (ex.isEmpty()) return@map p
            
            val sugarTsp = ex.remove(SUGAR_KEY) ?: 0
            
            // Rebuild product with extras
            // Note: Since Product properties might be val, we need a copy function or recreate it
            // Product(time = p.time, name = p.name, calories = p.calories, weight = p.weight, ingredients = p.ingredients, healthRating = p.healthRating, imageId = p.imageId, addedSugarTsp = p.addedSugarTsp + sugarTsp.toFloat(), extras = ex)
            
            // To be robust, let's create a copy method equivalent logic
            Product(
                time = p.time,
                name = p.name,
                calories = p.calories,
                weight = p.weight,
                ingredients = p.ingredients,
                healthRating = p.healthRating,
                imageId = p.imageId,
                addedSugarTsp = p.addedSugarTsp + sugarTsp.toFloat(),
                extras = ex
            )
        }
    }

    fun totalExtrasCalories(products: List<Product>): Int {
        return products.sumOf { p ->
            p.extras.entries.sumOf { (key, count) ->
                val def = definitions[key]
                (def?.second ?: 0) * count
            }
        }
    }

    private fun load(): MutableMap<String, MutableMap<String, Int>> {
        val json = prefs.getString(KEY_EXTRAS, null) ?: return mutableMapOf()
        return try {
            Json.decodeFromString(json)
        } catch (e: Exception) {
            mutableMapOf()
        }
    }

    private fun save(store: Map<String, Map<String, Int>>) {
        try {
            val json = Json.encodeToString(store)
            prefs.edit().putString(KEY_EXTRAS, json).apply()
        } catch (e: Exception) {
            // Ignore
        }
    }
}
