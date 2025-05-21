package com.geobus.marrakech.api

import com.geobus.marrakech.model.Stop
import com.geobus.marrakech.model.TimeEstimation
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface de service pour les requêtes liées aux stations de bus
 */
interface StopService {

    /**
     * Récupère toutes les stations de bus à Marrakech
     */
    @GET("stops/marrakech")
    suspend fun getAllStopsInMarrakech(): Response<List<Stop>>

    /**
     * Trouve la station la plus proche des coordonnées fournies
     */
    @GET("stops/nearest")
    suspend fun getNearestStop(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Response<Stop>

    /**
     * Calcule le temps estimé pour atteindre une station spécifique
     */
    @GET("stops/time")
    suspend fun getTimeToStop(
        @Query("userLat") userLat: Double,
        @Query("userLon") userLon: Double,
        @Query("stopId") stopId: Long
    ): Response<TimeEstimation>
}