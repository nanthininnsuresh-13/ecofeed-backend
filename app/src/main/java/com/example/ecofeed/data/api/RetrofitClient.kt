package com.example.ecofeed.data.api

import com.example.ecofeed.data.network.EcoFeedApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Principal Android Architect's Note:
 * This client is now connected to the live Render cloud backend.
 * Timeouts are extended to 60s to handle free-tier cold starts.
 */
object RetrofitClient {

    private const val BASE_URL = "https://ecofeed-backend.onrender.com/api/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(90, TimeUnit.SECONDS)
        .readTimeout(90, TimeUnit.SECONDS)
        .writeTimeout(90, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val api: EcoFeedApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
            .create(EcoFeedApiService::class.java)
    }
}
