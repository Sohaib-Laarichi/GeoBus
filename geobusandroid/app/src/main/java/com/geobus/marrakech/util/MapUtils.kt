package com.geobus.marrakech.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import androidx.core.content.ContextCompat
import com.geobus.marrakech.R
import com.geobus.marrakech.model.Stop
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*

/**
 * Classe utilitaire pour les opérations liées à Google Maps
 */
object MapUtils {

    /**
     * Position par défaut (centre de Marrakech)
     */
    val DEFAULT_MARRAKECH_LOCATION = LatLng(31.6295, -7.9811)

    /**
     * Niveau de zoom par défaut pour voir la ville
     */
    const val DEFAULT_ZOOM = 13f

    /**
     * Niveau de zoom rapproché pour voir le détail
     */
    const val DETAIL_ZOOM = 16f

    /**
     * Crée une icône de marqueur à partir d'un drawable vectoriel
     */
    fun getBitmapFromVector(context: Context, vectorResId: Int): BitmapDescriptor {
        val vectorDrawable = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable!!.setBounds(0, 0, vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(
            vectorDrawable.intrinsicWidth,
            vectorDrawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        vectorDrawable.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    /**
     * Centre la carte sur une position
     */
    fun centerMapOnLocation(googleMap: GoogleMap, location: LatLng, zoom: Float = DEFAULT_ZOOM) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoom))
    }

    /**
     * Ajoute un marqueur pour la position actuelle de l'utilisateur
     */
    fun addUserLocationMarker(
        context: Context,
        googleMap: GoogleMap,
        location: Location
    ): Marker {
        val latLng = LatLng(location.latitude, location.longitude)
        val markerOptions = MarkerOptions()
            .position(latLng)
            .title("Ma position")
            .icon(getBitmapFromVector(context, R.drawable.ic_my_location))
            .zIndex(2.0f) // Afficher au-dessus des autres marqueurs

        return googleMap.addMarker(markerOptions)!!
    }

    /**
     * Ajoute des marqueurs pour toutes les stations de bus
     */
    fun addStopMarkers(
        context: Context,
        googleMap: GoogleMap,
        stops: List<Stop>
    ): List<Marker> {
        val markers = mutableListOf<Marker>()

        stops.forEach { stop ->
            val latLng = LatLng(stop.latitude, stop.longitude)
            val markerOptions = MarkerOptions()
                .position(latLng)
                .title(stop.stopName)
                .snippet("Station de bus")
                .icon(getBitmapFromVector(context, R.drawable.ic_bus_stop))
                .zIndex(1.0f)

            val marker = googleMap.addMarker(markerOptions)!!
            marker.tag = stop.stopId
            markers.add(marker)
        }

        return markers
    }

    /**
     * Ajoute un marqueur spécial pour la station la plus proche
     */
    fun addNearestStopMarker(
        context: Context,
        googleMap: GoogleMap,
        stop: Stop
    ): Marker {
        val latLng = LatLng(stop.latitude, stop.longitude)
        val markerOptions = MarkerOptions()
            .position(latLng)
            .title(stop.stopName)
            .snippet("Station la plus proche")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            .zIndex(1.5f) // Au-dessus des stations normales mais sous l'utilisateur

        val marker = googleMap.addMarker(markerOptions)!!
        marker.tag = stop.stopId
        return marker
    }

    /**
     * Formate la distance en chaîne de caractères lisible
     */
    fun formatDistance(distanceMeters: Double): String {
        return when {
            distanceMeters < 1000 -> "${distanceMeters.toInt()} m"
            else -> String.format("%.1f km", distanceMeters / 1000)
        }
    }

    /**
     * Formate le temps de marche en chaîne de caractères lisible
     */
    fun formatWalkingTime(walkingTimeMinutes: Double): String {
        return when {
            walkingTimeMinutes < 1 -> "moins d'une minute"
            walkingTimeMinutes < 60 -> "${walkingTimeMinutes.toInt()} min"
            else -> {
                val hours = walkingTimeMinutes.toInt() / 60
                val mins = walkingTimeMinutes.toInt() % 60
                if (mins > 0) "$hours h $mins min" else "$hours h"
            }
        }
    }
}