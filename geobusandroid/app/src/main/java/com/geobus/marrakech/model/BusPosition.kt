package com.geobus.marrakech.model

import com.google.gson.annotations.SerializedName

/**
 * Modèle représentant la position d'un bus
 * Correspond au modèle backend Java
 */
data class BusPosition(
    @SerializedName("id")
    val id: Long? = null,

    @SerializedName("busId")
    val busId: String,

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("ligne")
    val ligne: String,

    @SerializedName("destination")
    val destination: String? = null, // Peut être null car non présent dans le backend

    @SerializedName("timestamp")
    val timestamp: String, // Format ISO datetime

    // Champs calculés côté client
    var minutesUntilArrival: Int? = null,
    var distanceToStop: Double? = null
) {

    /**
     * Calcule la distance vers un point donné
     */
    fun distanceTo(lat: Double, lon: Double): Double {
        val R = 6371000.0 // Rayon de la Terre en mètres

        val latDistance = Math.toRadians(lat - this.latitude)
        val lonDistance = Math.toRadians(lon - this.longitude)
        val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(lat)) *
                Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        return R * c
    }

    /**
     * Estime le temps d'arrivée en minutes basé sur la distance
     */
    fun estimateArrivalMinutes(stopLat: Double, stopLon: Double, avgSpeedKmh: Double = 25.0): Int {
        val distanceKm = distanceTo(stopLat, stopLon) / 1000.0
        val timeHours = distanceKm / avgSpeedKmh
        return (timeHours * 60).toInt().coerceAtLeast(1) // Minimum 1 minute
    }

    /**
     * Génère une destination basée sur la ligne si pas disponible
     */
    fun getDisplayDestination(): String {
        return destination ?: when {
            ligne.contains("1") -> "Centre-ville"
            ligne.contains("2") -> "Palmeraie"
            ligne.contains("3") -> "Aéroport"
            ligne.contains("4") -> "Université"
            ligne.contains("5") -> "Al Massira"
            ligne.contains("6") -> "Majorelle"
            ligne.contains("7") -> "Hivernage"
            ligne.contains("8") -> "Gare Routière"
            ligne.contains("Nuit") -> "Centre-ville"
            else -> "Destination inconnue"
        }
    }
}