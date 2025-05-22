package com.geobus.marrakech.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import androidx.core.content.ContextCompat
import com.geobus.marrakech.R
import com.geobus.marrakech.model.BusPosition
import com.geobus.marrakech.model.Stop
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*

/**
 * Utilitaires pour la gestion de Google Maps
 */
object MapUtils {

    val DEFAULT_MARRAKECH_LOCATION = LatLng(31.6295, -7.9811)
    const val DEFAULT_ZOOM = 12f
    const val DETAIL_ZOOM = 15f

    /**
     * Centre la carte sur une localisation donnée
     */
    fun centerMapOnLocation(map: GoogleMap, location: LatLng, zoom: Float = DEFAULT_ZOOM) {
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, zoom))
    }

    /**
     * Ajoute un marqueur pour la position de l'utilisateur
     */
    fun addUserLocationMarker(context: Context, map: GoogleMap, location: Location): Marker? {
        val latLng = LatLng(location.latitude, location.longitude)
        return map.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Ma position")
                .icon(getBitmapFromVector(context, R.drawable.ic_my_location))
        )
    }

    /**
     * Ajoute des marqueurs pour toutes les stations
     */
    fun addStopMarkers(context: Context, map: GoogleMap, stops: List<Stop>): List<Marker> {
        return stops.mapNotNull { stop ->
            map.addMarker(
                MarkerOptions()
                    .position(LatLng(stop.latitude, stop.longitude))
                    .title(stop.stopName)
                    .icon(getBitmapFromVector(context, R.drawable.ic_bus_stop))
            )?.also { marker ->
                marker.tag = stop.stopId
            }
        }
    }

    /**
     * Ajoute un marqueur spécial pour la station la plus proche
     */
    fun addNearestStopMarker(context: Context, map: GoogleMap, stop: Stop): Marker? {
        return map.addMarker(
            MarkerOptions()
                .position(LatLng(stop.latitude, stop.longitude))
                .title("Station la plus proche: ${stop.stopName}")
                .icon(getBitmapFromVector(context, R.drawable.ic_nearest_stop))
        )
    }

    /**
     * Ajoute des marqueurs pour les bus avec style amélioré
     */
    fun addBusMarkers(context: Context, map: GoogleMap, buses: List<BusPosition>): List<Marker> {
        return buses.mapNotNull { bus ->
            // Créer un titre plus informatif
            val title = "Bus ${bus.busId} - Ligne ${bus.ligne}"

            // Créer un snippet avec plus d'informations
            val snippet = buildString {
                append("Destination: ${bus.getDisplayDestination()}")
                bus.minutesUntilArrival?.let { minutes ->
                    append("\nArrivée: ")
                    when {
                        minutes <= 1 -> append("Imminent")
                        else -> append("Dans $minutes min")
                    }
                }
                bus.distanceToStop?.let { distance ->
                    val distanceText = if (distance < 1000) {
                        "${distance.toInt()} m"
                    } else {
                        String.format("%.1f", distance / 1000) + " km"
                    }
                    append("\nDistance: $distanceText")
                }
            }

            map.addMarker(
                MarkerOptions()
                    .position(LatLng(bus.latitude, bus.longitude))
                    .title(title)
                    .snippet(snippet)
                    .icon(getBitmapFromVector(context, R.drawable.ic_bus))
                    .zIndex(2.0f)  // Mettre les bus au-dessus des autres marqueurs
            )?.also { marker ->
                marker.tag = bus.busId
            }
        }
    }

    /**
     * Convertit un drawable vectoriel en BitmapDescriptor
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
}
