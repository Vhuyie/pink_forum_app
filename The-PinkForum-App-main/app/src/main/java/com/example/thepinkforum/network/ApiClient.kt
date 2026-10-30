package com.example.thepinkforum.network

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    // 10.0.2.11 points to your computer's local WAMP server from inside the Android Emulator
    // Update the path to match wherever your PHP API files live inside the WAMP www/ folder
    private const val BASE_URL = "https://the-pinkforum-website-production-8217.up.railway.app/api/"

    private var retrofit: Retrofit? = null

    // Initialize Retrofit with the Context so it can access TokenManager
    fun getService(context: Context): ApiService {
        if (retrofit == null) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            // Add the new AuthInterceptor here!
            val authInterceptor = AuthInterceptor(context)

            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(authInterceptor)// Automatically injects JWT
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }

        return retrofit!!.create(ApiService::class.java)
    }
}