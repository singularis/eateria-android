package com.singularis.eateria.services

import android.content.Context
import android.util.Log
import okhttp3.Interceptor

class AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val token = TokenStore.read(context)
        val request = if (token != null) {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}

class AuthResponseInterceptor(
    private val context: Context,
    private val onUnauthorized: () -> Unit
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        val response = chain.proceed(chain.request())
        if (response.code == 401) {
            Log.e("AuthResponseInterceptor", "Received 401, clearing token")
            TokenStore.clear(context)
            onUnauthorized()
        }
        return response
    }
}
