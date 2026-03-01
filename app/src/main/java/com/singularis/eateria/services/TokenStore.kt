package com.singularis.eateria.services

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object TokenStore {
    private const val PREFS_FILE = "secure_prefs"
    private const val KEY_JWT = "jwt_token"

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

    fun save(context: Context, token: String) {
        prefs(context).edit().putString(KEY_JWT, token).apply()
    }

    fun read(context: Context): String? =
        prefs(context).getString(KEY_JWT, null)

    fun clear(context: Context) {
        prefs(context).edit().remove(KEY_JWT).apply()
    }
}
