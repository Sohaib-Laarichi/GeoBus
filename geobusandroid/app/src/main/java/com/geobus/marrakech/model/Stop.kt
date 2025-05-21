package com.geobus.marrakech.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Modèle représentant une station de bus
 */
@Parcelize
data class Stop(
    val stopId: Long,
    val stopName: String,
    val latitude: Double,
    val longitude: Double,
    val ville: String,
    var distance: Double? = null,
    var walkingTimeMinutes: Double? = null
) : Parcelable {

    /**
     * Calcule la distance entre cette station et les coordonnées données
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
        return R * c * 1000 // Distance en mètres
    }

    /**
     * Estime le temps nécessaire pour marcher jusqu'à cette station
     * en considérant une vitesse moyenne de marche de 5 km/h
     *
     * @param lat Latitude du point de départ
     * @param lon Longitude du point de départ
     * @return Temps estimé en minutes
     */
    fun estimateWalkingTime(lat: Double, lon: Double): Double {
        val distance = distanceTo(lat, lon)
        // Considérer une vitesse moyenne de marche de 5 km/h
        val walkingSpeedMetersPerMinute = 5 * 1000 / 60 // 5km/h en m/min
        return distance / walkingSpeedMetersPerMinute
    }
}