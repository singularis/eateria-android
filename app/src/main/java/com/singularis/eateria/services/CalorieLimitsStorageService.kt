package com.singularis.eateria.services

import android.content.Context
import com.google.gson.Gson
import java.io.File

/**
 * Persist and retrieve user calorie limits using a JSON file in internal storage.
 * We avoid relying solely on SharedPreferences to ensure the data does not get purged and
 * remains independent of transient memory; JSON file is human-inspectable for debugging.
 */
class CalorieLimitsStorageService private constructor(private val context: Context) {

    data class Limits(
        val softLimit: Int,
        val hardLimit: Int,
        val hasManualCalorieLimits: Boolean
    )

    private val fileName = "calorie_limits.json"
    private val gson = Gson()

    private val fileURL: File
        get() = File(context.filesDir, fileName)

    fun load(): Limits? {
        return try {
            val file = fileURL
            if (!file.exists()) return null
            val data = file.readText()
            gson.fromJson(data, Limits::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun save(limits: Limits) {
        try {
            val data = gson.toJson(limits)
            fileURL.writeText(data)
        } catch (e: Exception) {
            // Ignore
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: CalorieLimitsStorageService? = null

        fun getInstance(context: Context): CalorieLimitsStorageService =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: CalorieLimitsStorageService(context.applicationContext).also { INSTANCE = it }
            }
    }
}
