package com.example.core.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClientProvider {
    fun createService(baseUrl: String): FastApiEdgeService? {
        if (baseUrl.isBlank()) return null
        return try {
            // Handle trailing slash guarantee in Retrofit (base URL must end in '/')
            val cleanUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
            
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            
            val okHttpClient = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .build()

            Retrofit.Builder()
                .baseUrl(cleanUrl)
                .client(okHttpClient)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(FastApiEdgeService::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
