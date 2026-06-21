package com.singularis.eateria.services

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object TokenStore {
    private const val PREFS_FILE = "secure_prefs"
    const val KEY_JWT = "jwt_token"

    private fun prefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // Generic string operations
    fun save(context: Context, value: String, key: String) {
        prefs(context).edit().putString(key, value).apply()
    }

    fun read(context: Context, key: String): String? =
        prefs(context).getString(key, null)

    // Legacy Token operations mapped to generic
    fun save(context: Context, token: String) {
        save(context, token, KEY_JWT)
    }

    fun read(context: Context): String? =
        read(context, KEY_JWT)

    fun clear(context: Context) {
        prefs(context).edit().remove(KEY_JWT).apply()
    }

    // Boolean operations matching iOS
    fun setBool(context: Context, value: Boolean, key: String) {
        save(context, if (value) "true" else "false", key)
    }

    fun getBool(context: Context, key: String): Boolean =
        read(context, key) == "true"

    fun clearAll(context: Context) {
        prefs(context).edit().clear().apply()
    }
}
