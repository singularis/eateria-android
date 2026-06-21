package com.singularis.eateria.services

import android.content.Context
import android.content.SharedPreferences

class AppEnvironment private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var useDevEnvironment: Boolean
        get() = prefs.getBoolean(USE_DEV_ENV_KEY, false) // Default to false in Android
        set(value) {
            prefs.edit().putBoolean(USE_DEV_ENV_KEY, value).apply()
        }

    val baseURL: String
        get() = if (useDevEnvironment) "https://chater.singularis.work/dev" else "https://chater.singularis.work"

    val autocompleteBaseURL: String
        get() = if (useDevEnvironment) "https://chater.singularis.work/dev" else "https://chater.singularis.work"

    val webSocketURL: String
        get() = if (useDevEnvironment) "wss://chater.singularis.work/dev/autocomplete" else "wss://chater.singularis.work/autocomplete"

    val sessionCookiePrefix: String
        get() = if (useDevEnvironment) "_dev:chater_ui:" else "chater_ui:"

    companion object {
        private const val PREFS_NAME = "app_environment"
        private const val USE_DEV_ENV_KEY = "use_dev_environment"

        @Volatile
        private var instance: AppEnvironment? = null

        fun initialize(context: Context) {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = AppEnvironment(context.applicationContext)
                    }
                }
            }
        }

        fun getInstance(): AppEnvironment {
            return instance ?: throw IllegalStateException("AppEnvironment must be initialized first")
        }
    }
}
