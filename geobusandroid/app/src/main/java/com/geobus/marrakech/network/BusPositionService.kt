package com.geobus.marrakech.network

import com.geobus.marrakech.model.BusPosition
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Interface pour les requêtes liées aux positions des bus
 */
interface BusPositionService {
    
    /**
     * Récupère toutes les dernières positions des bus
     * 
     * @return Liste des positions des bus
     */
    @GET("positions")
    suspend fun getAllLatestPositions(): List<BusPosition>
    
    /**
     * Récupère les bus qui se dirigent vers un arrêt spécifique
     * 
     * @param stopId Identifiant de l'arrêt
     * @return Liste des positions des bus allant vers cet arrêt
     */
    @GET("positions/stop/{stopId}")
    suspend fun getBusesGoingToStop(@Path("stopId") stopId: Long): List<BusPosition>
    
    /**
     * Ajoute une nouvelle position de bus
     * 
     * @param busPosition Position du bus à ajouter
     * @return Position du bus enregistrée
     */
    @POST("positions")
    suspend fun addBusPosition(@Body busPosition: BusPosition): BusPosition
}