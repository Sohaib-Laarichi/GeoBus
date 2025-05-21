package com.geobus.marrakech.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Client API pour les requêtes réseau
 */
object ApiClient {
    
    // URL de base de l'API
    private const val BASE_URL = "http://10.0.2.2:8080/"
    
    // Client HTTP avec logging et timeouts
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    // Instance Retrofit
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    
    // Services API
    val authService: AuthService by lazy {
        retrofit.create(AuthService::class.java)
    }
    
    val stopService: StopService by lazy {
        retrofit.create(StopService::class.java)
    }
    
    val busPositionService: BusPositionService by lazy {
        retrofit.create(BusPositionService::class.java)
    }
}