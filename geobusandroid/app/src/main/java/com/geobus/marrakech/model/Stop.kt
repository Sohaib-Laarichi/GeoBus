package com.geobus.marrakech.model

import com.google.gson.annotations.SerializedName
import kotlin.math.*

/**
 * Modèle représentant une station de bus
 */
data class Stop(
    @SerializedName("stopId")
    val stopId: Long,

    @SerializedName("stopName")
    val stopName: String,

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("ville")
    val ville: String,

    // Champs calculés côté client
    var distance: Double? = null,
    var walkingTimeMinutes: Double? = null
) {

    /**
     * Calcule la distance vers un point donné
     */
    fun distanceTo(lat: Double, lon: Double): Double {
        val R = 6371000.0 // Rayon de la Terre en mètres

        val latDistance = Math.toRadians(lat - this.latitude)
        val lonDistance = Math.toRadians(lon - this.longitude)
        val a = sin(latDistance / 2) * sin(latDistance / 2) +
                cos(Math.toRadians(this.latitude)) * cos(Math.toRadians(lat)) *
                sin(lonDistance / 2) * sin(lonDistance / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return R * c
    }

    /**
     * Estime le temps de marche en minutes
     */
    fun estimateWalkingTime(fromLat: Double, fromLon: Double, walkingSpeedKmh: Double = 5.0): Double {
        val distanceKm = distanceTo(fromLat, fromLon) / 1000.0
        return (distanceKm / walkingSpeedKmh) * 60.0 // Conversion en minutes
    }
}