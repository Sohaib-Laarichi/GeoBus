package com.geobus.marrakech.network

import com.geobus.marrakech.model.Stop
import com.geobus.marrakech.model.TimeEstimation
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface pour les requêtes liées aux stations de bus
 */
interface StopService {

    @GET("stops/marrakech")
    suspend fun getAllStopsInMarrakech(): List<Stop>

    @GET("stops/nearest")
    suspend fun getNearestStop(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Stop

    @GET("stops/time")
    suspend fun getTimeToStop(
        @Query("userLat") userLat: Double,
        @Query("userLon") userLon: Double,
        @Query("stopId") stopId: Long
    ): TimeEstimation
}