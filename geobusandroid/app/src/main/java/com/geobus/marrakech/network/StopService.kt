package com.geobus.marrakech.network

import com.geobus.marrakech.model.Stop
import com.geobus.marrakech.model.TimeEstimation
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface pour les requêtes liées aux stations de bus
 */
interface StopService {
    
    /**
     * Récupère toutes les stations de bus à Marrakech
     * 
     * @return Liste des stations
     */
    @GET("stops/marrakech")
    suspend fun getAllStopsInMarrakech(): List<Stop>
    
    /**
     * Trouve la station la plus proche des coordonnées fournies
     * 
     * @param lat Latitude de l'utilisateur
     * @param lon Longitude de l'utilisateur
     * @return La station la plus proche
     */
    @GET("stops/nearest")
    suspend fun getNearestStop(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Stop
    
    /**
     * Calcule le temps estimé pour atteindre une station spécifique
     * 
     * @param userLat Latitude de l'utilisateur
     * @param userLon Longitude de l'utilisateur
     * @param stopId Identifiant de la station
     * @return Informations sur le temps de trajet
     */
    @GET("stops/time")
    suspend fun getTimeToStop(
        @Query("userLat") userLat: Double,
        @Query("userLon") userLon: Double,
        @Query("stopId") stopId: Long
    ): TimeEstimation
}