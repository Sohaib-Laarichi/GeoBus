package com.geobus.marrakech.repository

import android.util.Log
import com.geobus.marrakech.api.ApiClient
import com.geobus.marrakech.api.ApiService
import com.geobus.marrakech.model.BusPosition
import retrofit2.Response

class BusPositionRepository {

    private val apiService: ApiService = ApiClient.getApiService()
    private val TAG = "BusPositionRepo"

    /**
     * Récupère toutes les positions récentes des bus
     */
    suspend fun getAllLatestPositions(): List<BusPosition>? {
        return try {
            Log.d(TAG, "Récupération de toutes les positions récentes")
            val response: Response<List<BusPosition>> = apiService.getAllBusPositions()

            if (response.isSuccessful) {
                val positions = response.body()
                Log.d(TAG, "Positions récupérées: ${positions?.size ?: 0}")

                if (positions.isNullOrEmpty()) {
                    Log.w(TAG, "Aucune position de bus retournée par l'API")
                    // Créer des positions de test si l'API ne retourne rien
                    return createTestBusPositions()
                }

                positions
            } else {
                Log.e(TAG, "Erreur API: ${response.code()} - ${response.message()}")
                // Créer des positions de test en cas d'erreur
                createTestBusPositions()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur récupération positions: ${e.message}")
            // Créer des positions de test en cas d'exception
            createTestBusPositions()
        }
    }

    /**
     * Crée des positions de bus de test pour le développement
     */
    private fun createTestBusPositions(): List<BusPosition> {
        Log.d(TAG, "Création de positions de bus de test")
        val testPositions = listOf(
            BusPosition(
                id = 1L,
                busId = "BUS001",
                latitude = 31.6295,
                longitude = -7.9811,
                ligne = "1",
                destination = "Centre-ville",
                timestamp = "2023-01-01T12:00:00Z"
            ),
            BusPosition(
                id = 2L,
                busId = "BUS002",
                latitude = 31.6350,
                longitude = -7.9900,
                ligne = "2",
                destination = "Gueliz",
                timestamp = "2023-01-01T12:00:00Z"
            ),
            BusPosition(
                id = 3L,
                busId = "BUS003",
                latitude = 31.6200,
                longitude = -7.9750,
                ligne = "3",
                destination = "Médina",
                timestamp = "2023-01-01T12:00:00Z"
            )
        )
        Log.d(TAG, "Positions de test créées: ${testPositions.size}")
        return testPositions
    }

    /**
     * Récupère les bus se dirigeant vers une station spécifique
     */
    suspend fun getBusesGoingToStop(stopId: Long): List<BusPosition>? {
        return try {
            Log.d(TAG, "Récupération des bus pour station $stopId")
            val response: Response<List<BusPosition>> = apiService.getBusesForStop(stopId)

            if (response.isSuccessful) {
                val buses = response.body()
                Log.d(TAG, "Bus trouvés pour station $stopId: ${buses?.size ?: 0}")

                if (buses.isNullOrEmpty()) {
                    Log.w(TAG, "Aucun bus trouvé pour la station $stopId, création de bus de test")
                    return createTestBusesForStop(stopId)
                }

                buses
            } else {
                Log.e(TAG, "Erreur récupération bus pour station: ${response.code()} - ${response.message()}")
                createTestBusesForStop(stopId)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur récupération bus pour station: ${e.message}")
            createTestBusesForStop(stopId)
        }
    }

    /**
     * Crée des bus de test pour une station spécifique
     */
    private fun createTestBusesForStop(stopId: Long): List<BusPosition> {
        Log.d(TAG, "Création de bus de test pour la station $stopId")

        // Coordonnées fictives pour la station
        val stopLat = 31.6295 + (stopId % 10) * 0.001
        val stopLon = -7.9811 - (stopId % 5) * 0.001

        val testBuses = listOf(
            BusPosition(
                id = stopId * 10 + 1,
                busId = "BUS${stopId}01",
                latitude = stopLat + 0.005,
                longitude = stopLon - 0.003,
                ligne = "${stopId % 5 + 1}",
                destination = "Station $stopId",
                timestamp = "2023-01-01T12:00:00Z"
            ),
            BusPosition(
                id = stopId * 10 + 2,
                busId = "BUS${stopId}02",
                latitude = stopLat - 0.002,
                longitude = stopLon + 0.004,
                ligne = "${(stopId + 1) % 5 + 1}",
                destination = "Station $stopId",
                timestamp = "2023-01-01T12:00:00Z"
            )
        ).map { bus ->
            // Calculer la distance et le temps d'arrivée
            val distance = bus.distanceTo(stopLat, stopLon)
            val minutes = bus.estimateArrivalMinutes(stopLat, stopLon)

            bus.copy(
                distanceToStop = distance,
                minutesUntilArrival = minutes
            )
        }

        Log.d(TAG, "Bus de test créés pour station $stopId: ${testBuses.size}")
        return testBuses
    }

    /**
     * Récupère les positions des bus d'une ligne spécifique
     */
    suspend fun getBusPositionsByLine(ligne: String): List<BusPosition>? {
        return try {
            Log.d(TAG, "Récupération des positions pour ligne $ligne")
            val response: Response<List<BusPosition>> = apiService.getBusPositionsByLine(ligne)

            if (response.isSuccessful) {
                val positions = response.body()
                Log.d(TAG, "Positions récupérées pour ligne $ligne: ${positions?.size ?: 0}")
                positions
            } else {
                Log.e(TAG, "Erreur API ligne $ligne: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Erreur récupération ligne $ligne: ${e.message}")
            null
        }
    }

    /**
     * Calcule les temps d'arrivée estimés pour une station
     */
    fun calculateArrivalTimes(buses: List<BusPosition>, stopLat: Double, stopLon: Double): List<BusPosition> {
        return buses.map { bus ->
            val distance = bus.distanceTo(stopLat, stopLon)
            val estimatedMinutes = bus.estimateArrivalMinutes(stopLat, stopLon)

            bus.copy(
                distanceToStop = distance,
                minutesUntilArrival = estimatedMinutes
            )
        }.sortedBy { it.minutesUntilArrival }
    }
}
