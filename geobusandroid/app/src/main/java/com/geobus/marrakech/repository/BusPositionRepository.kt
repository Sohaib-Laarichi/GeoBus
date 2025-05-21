package com.geobus.marrakech.repository

import com.geobus.marrakech.model.BusPosition
import com.geobus.marrakech.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Repository pour gérer les positions des bus
 */
class BusPositionRepository {
    
    private val apiClient = ApiClient.busPositionService
    
    /**
     * Récupère toutes les dernières positions des bus
     * 
     * @return Liste des positions des bus
     */
    suspend fun getAllLatestPositions(): List<BusPosition>? {
        return withContext(Dispatchers.IO) {
            try {
                apiClient.getAllLatestPositions()
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Récupère les bus qui se dirigent vers un arrêt spécifique
     * 
     * @param stopId Identifiant de l'arrêt
     * @return Liste des positions des bus allant vers cet arrêt
     */
    suspend fun getBusesGoingToStop(stopId: Long): List<BusPosition>? {
        return withContext(Dispatchers.IO) {
            try {
                val buses = apiClient.getBusesGoingToStop(stopId)
                
                // Calculer le temps d'arrivée estimé pour chaque bus
                buses.map { bus ->
                    // Le temps d'arrivée est déjà calculé par le backend
                    // Mais on peut ajouter le calcul des minutes restantes ici
                    val minutesUntilArrival = if (bus.estimatedArrivalTime != null) {
                        ChronoUnit.MINUTES.between(LocalDateTime.now(), bus.estimatedArrivalTime).toInt()
                    } else {
                        null
                    }
                    
                    // Créer une copie avec les minutes calculées
                    bus.copy(minutesUntilArrival = minutesUntilArrival)
                }
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Ajoute une nouvelle position de bus
     * 
     * @param busPosition Position du bus à ajouter
     * @return Position du bus enregistrée
     */
    suspend fun addBusPosition(busPosition: BusPosition): BusPosition? {
        return withContext(Dispatchers.IO) {
            try {
                apiClient.addBusPosition(busPosition)
            } catch (e: Exception) {
                null
            }
        }
    }
}