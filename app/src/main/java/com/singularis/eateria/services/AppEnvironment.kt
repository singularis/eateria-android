package com.singularis.eateria.services

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.singularis.eateria.BuildConfig

class AppEnvironment private constructor(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Dev API (`/dev`) is allowed only in debug builds.
     * Release / Play Store always uses production — prefs cannot override that.
     */
    var useDevEnvironment: Boolean
        get() {
            if (!BuildConfig.USE_DEV_API) return false
            if (!prefs.contains(USE_DEV_ENV_KEY)) return true
            return prefs.getBoolean(USE_DEV_ENV_KEY, true)
        }
        set(value) {
            if (!BuildConfig.USE_DEV_API) return
            prefs.edit().putBoolean(USE_DEV_ENV_KEY, value).apply()
        }

    val baseURL: String
        get() = if (useDevEnvironment) DEV_BASE else PROD_BASE

    val autocompleteBaseURL: String
        get() = baseURL

    val webSocketURL: String
        get() =
            if (useDevEnvironment) {
                "wss://chater.singularis.work/dev/autocomplete"
            } else {
                "wss://chater.singularis.work/autocomplete"
            }

    val sessionCookiePrefix: String
        get() = if (useDevEnvironment) "_dev:chater_ui:" else "chater_ui:"

    companion object {
        private const val TAG = "AppEnvironment"
        private const val PREFS_NAME = "app_environment"
        private const val USE_DEV_ENV_KEY = "use_dev_environment"
        private const val PROD_BASE = "https://chater.singularis.work"
        private const val DEV_BASE = "https://chater.singularis.work/dev"

        @Volatile
        private var instance: AppEnvironment? = null

        fun initialize(context: Context) {
            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        instance = AppEnvironment(context.applicationContext).also {
                            Log.i(
                                TAG,
                                "API base=${it.baseURL} useDev=${it.useDevEnvironment} " +
                                    "USE_DEV_API=${BuildConfig.USE_DEV_API} DEBUG=${BuildConfig.DEBUG}",
                            )
                        }
                    }
                }
            }
        }

        fun getInstance(): AppEnvironment {
            return instance ?: throw IllegalStateException("AppEnvironment must be initialized first")
        }
    }
}
