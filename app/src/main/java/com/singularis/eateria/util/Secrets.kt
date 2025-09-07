package com.singularis.eateria.util

import com.singularis.eateria.BuildConfig

object Secrets {
    val googleClientId: String
        get() = BuildConfig.GOOGLE_CLIENT_ID ?: "your_google_client_id_here"
}
