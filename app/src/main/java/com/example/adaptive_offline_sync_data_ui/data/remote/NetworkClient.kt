package com.example.adaptive_offline_sync_data_ui.data.remote

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkClient {
    // Note: Keep trailing slash for Retrofit; on Android Emulator use "http://10.0.2.2:8000/"
    // (or "http://localhost:8000/" if using `adb reverse tcp:8000 tcp:8000`)
    private const val BASE_URL = "http://localhost:8000/"

    @Volatile
    private var accessToken: String? = null

    fun setAccessToken(token: String?) {
        accessToken = token
    }

    fun getAccessToken(): String? = accessToken

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val token = accessToken
        val request = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }
        chain.proceed(request)
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val syncServiceApi: SyncServiceApi by lazy {
        retrofit.create(SyncServiceApi::class.java)
    }

    val recordsApi: RecordsAPI by lazy {
        retrofit.create(RecordsAPI::class.java)
    }

    val analyticsApiService: AnalyticsAPIService by lazy {
        retrofit.create(AnalyticsAPIService::class.java)
    }
}