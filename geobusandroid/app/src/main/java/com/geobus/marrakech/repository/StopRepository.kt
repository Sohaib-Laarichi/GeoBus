package com.geobus.marrakech.repository

import com.geobus.marrakech.api.ApiClient
import com.geobus.marrakech.model.Stop
import com.geobus.marrakech.model.TimeEstimation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository pour les opérations liées aux stations de bus
 */
class StopRepository {

    private val stopService = ApiClient.stopService
    private val apiService = ApiClient.getApiService()

    /**
     * Récupère toutes les stations de bus à Marrakech
     * @return Liste des stations ou null en cas d'erreur
     */
    suspend fun getAllStopsInMarrakech(): List<Stop>? = withContext(Dispatchers.IO) {
        try {
            val response = stopService.getAllStopsInMarrakech()
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Récupère une station par son ID
     * @return La station ou null en cas d'erreur
     */
    suspend fun getStopById(stopId: Long): Stop? = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getStopById(stopId)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Trouve la station la plus proche des coordonnées fournies
     * @return La station la plus proche ou null en cas d'erreur
     */
    suspend fun getNearestStop(lat: Double, lon: Double): Stop? = withContext(Dispatchers.IO) {
        try {
            val response = stopService.getNearestStop(lat, lon)
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Calcule le temps estimé pour atteindre une station spécifique
     * @return Informations sur le temps de trajet ou null en cas d'erreur
     */
    suspend fun getTimeToStop(userLat: Double, userLon: Double, stopId: Long): TimeEstimation? =
        withContext(Dispatchers.IO) {
            try {
                val response = stopService.getTimeToStop(userLat, userLon, stopId)
                if (response.isSuccessful) {
                    response.body()
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
}
