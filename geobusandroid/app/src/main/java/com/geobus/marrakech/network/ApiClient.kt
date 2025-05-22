package com.geobus.marrakech.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Client API pour les requêtes réseau - CORRIGÉ
 */
object ApiClient {

    // CORRECTION: URL mise à jour pour correspondre au backend
    // Pour émulateur Android: 10.0.2.2 = localhost du PC hôte
    // Pour appareil physique: remplacez par l'IP de votre PC (ex: 192.168.1.100)
    private const val BASE_URL = "http://10.0.2.2:8080/"

    // Client HTTP avec logging et timeouts améliorés
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .connectTimeout(60, TimeUnit.SECONDS)  // Augmenté pour debug
        .readTimeout(60, TimeUnit.SECONDS)     // Augmenté pour debug
        .writeTimeout(60, TimeUnit.SECONDS)    // Augmenté pour debug
        .retryOnConnectionFailure(true)        // Retry automatique
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