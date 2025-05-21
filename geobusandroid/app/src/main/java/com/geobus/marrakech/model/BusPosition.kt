package com.geobus.marrakech.model

import java.time.LocalDateTime

/**
 * Modèle représentant la position d'un bus à un moment donné
 */
data class BusPosition(
    val id: Long? = null,
    val busId: String,
    val latitude: Double,
    val longitude: Double,
    val ligne: String,
    val destination: String,
    val timestamp: LocalDateTime,
    
    // Champs calculés
    val estimatedArrivalTime: LocalDateTime? = null,
    val minutesUntilArrival: Int? = null
) {
    /**
     * Calcule la distance entre ce bus et les coordonnées données
     * en utilisant la formule de Haversine (distance sur la surface d'une sphère)
     *
     * @param lat Latitude du point à comparer
     * @param lon Longitude du point à comparer
     * @return Distance en mètres
     */
    fun distanceTo(lat: Double, lon: Double): Double {
        val R = 6371 // Rayon de la Terre en km
        val latDistance = Math.toRadians(lat - this.latitude)
        val lonDistance = Math.toRadians(lon - this.longitude)
        val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(lat)) *
                Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val distance = R * c * 1000 // Distance en mètres

        return distance
    }
}