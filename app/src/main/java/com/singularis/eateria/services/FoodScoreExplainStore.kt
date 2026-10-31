package com.singularis.eateria.services

import android.content.Context
import org.json.JSONObject

/** Saved explain text. Reused only while the dish time it was based on still matches. */
class FoodScoreExplainStore private constructor(context: Context) {
    data class Entry(val text: String, val basisDishTime: Long)

    private val prefs = context.applicationContext.getSharedPreferences(STORAGE, Context.MODE_PRIVATE)

    fun dayEntry(email: String, day: String): Entry? = read(dayKey(email, day))

    fun saveDay(email: String, day: String, text: String, basisDishTime: Long) {
        write(dayKey(email, day), text, basisDishTime)
    }

    fun dishEntry(email: String, dishTime: Long): Entry? = read(dishKey(email, dishTime))

    fun saveDish(email: String, dishTime: Long, text: String, basisDishTime: Long) {
        write(dishKey(email, dishTime), text, basisDishTime)
    }

    private fun dayKey(email: String, day: String) = "day|$email|$day"

    private fun dishKey(email: String, dishTime: Long) = "dish|$email|$dishTime"

    private fun read(key: String): Entry? {
        val raw = prefs.getString(key, null) ?: return null
        return try {
            val json = JSONObject(raw)
            Entry(json.getString("text"), json.getLong("basis"))
        } catch (_: Exception) {
            null
        }
    }

    private fun write(key: String, text: String, basisDishTime: Long) {
        val json = JSONObject().put("text", text).put("basis", basisDishTime)
        prefs.edit().putString(key, json.toString()).apply()
    }

    companion object {
        private const val STORAGE = "food_score_explain_cache_v1"

        @Volatile
        private var instance: FoodScoreExplainStore? = null

        fun getInstance(context: Context): FoodScoreExplainStore {
            return instance ?: synchronized(this) {
                instance ?: FoodScoreExplainStore(context.applicationContext).also { instance = it }
            }
        }
    }
}
