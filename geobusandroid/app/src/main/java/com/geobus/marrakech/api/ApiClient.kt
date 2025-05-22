package com.geobus.marrakech.api

import com.geobus.marrakech.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Client API pour communiquer avec le backend Spring Boot
 */
object ApiClient {

    private const val TIMEOUT_SECONDS = 30L

    /**
     * Crée une instance du client OkHttp configurée
     */
    private fun createOkHttpClient(): OkHttpClient {
        val interceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    /**
     * Instance Retrofit partagée
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Service pour les stations de bus (existant)
     */
    val stopService: StopService by lazy {
        retrofit.create(StopService::class.java)
    }

    /**
     * Service pour les positions de bus (nouveau)
     */
    private val busPositionService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    /**
     * Méthode pour obtenir le service API (pour compatibilité avec BusPositionRepository)
     */
    fun getApiService(): ApiService = busPositionService
}