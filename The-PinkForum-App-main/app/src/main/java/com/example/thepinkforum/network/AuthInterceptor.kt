package com.example.thepinkforum.network

import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(context: Context) : Interceptor {
    private val tokenManager = TokenManager(context)

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()

        // Retrieve the token
        val token = tokenManager.getToken()

        // If the token exists, attach it to the headers
        if (token != null) {
            Log.d("AUTH_INTERCEPTOR", "Adding Token to request: ${chain.request().url}")
            requestBuilder.addHeader("Authorization", "Bearer $token")
        } else {
            Log.w("AUTH_INTERCEPTOR", "No Token found for request: ${chain.request().url}")
        }

        return chain.proceed(requestBuilder.build())
    }
}
