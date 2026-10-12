package com.jarvis.assistant.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface JarvisApiService {
    @GET("health")
    suspend fun healthCheck(): Response<Map<String, String>>

    @POST("api/chat")
    suspend fun sendMessage(@Body request: ChatRequest): Response<ChatResponse>

    @POST("api/summarize")
    suspend fun summarizeDocument(@Body request: SummarizeRequest): Response<SummarizeResponse>

    companion object {
        fun create(baseUrl: String): JarvisApiService {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .connectTimeout(25, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(25, TimeUnit.SECONDS)
                .addInterceptor(loggingInterceptor)
                .build()

            val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

            return Retrofit.Builder()
                .baseUrl(normalizedUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(JarvisApiService::class.java)
        }
    }
}
