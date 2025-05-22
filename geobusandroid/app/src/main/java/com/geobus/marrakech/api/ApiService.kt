package com.geobus.marrakech.api

import com.geobus.marrakech.model.BusPosition
import com.geobus.marrakech.model.Stop
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Interface pour les appels API
 */
interface ApiService {

    // =================== ENDPOINTS POSITIONS ===================

    /**
     * Récupère toutes les positions récentes des bus
     */
    @GET("positions")
    suspend fun getAllBusPositions(
        @Query("minutes") minutes: Int = 30
    ): Response<List<BusPosition>>

    /**
     * Récupère les bus pour une station spécifique
     */
    @GET("positions/stop/{stopId}")
    suspend fun getBusesForStop(
        @Path("stopId") stopId: Long
    ): Response<List<BusPosition>>

    /**
     * Test de connectivité pour les positions
     */
    @GET("positions/test")
    suspend fun testPositionsEndpoint(): Response<String>

    /**
     * Compte le nombre de positions
     */
    @GET("positions/count")
    suspend fun getPositionsCount(): Response<Long>

    // =================== ENDPOINTS BUSES ===================

    /**
     * Récupère les positions des bus d'une ligne
     */
    @GET("buses/line/{ligne}/positions")
    suspend fun getBusPositionsByLine(
        @Path("ligne") ligne: String,
        @Query("minutes") minutes: Int = 30
    ): Response<List<BusPosition>>

    /**
     * Récupère la dernière position d'un bus spécifique
     */
    @GET("buses/{busId}/position")
    suspend fun getLastBusPosition(
        @Path("busId") busId: String
    ): Response<BusPosition>

    /**
     * Test de connectivité pour les bus
     */
    @GET("buses/test")
    suspend fun testBusesEndpoint(): Response<String>

    // =================== ENDPOINTS STOPS ===================

    /**
     * Récupère toutes les stations
     */
    @GET("stops")
    suspend fun getAllStops(): Response<List<Stop>>

    /**
     * Récupère une station par ID
     */
    @GET("stops/{id}")
    suspend fun getStopById(@Path("id") id: Long): Response<Stop>
}