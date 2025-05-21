package com.geobus.marrakech.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*

/**
 * Gestionnaire de localisation pour obtenir la position de l'utilisateur
 */
class LocationManager(private val context: Context) {

    private val _locationLiveData = MutableLiveData<Location?>()
    val locationLiveData: LiveData<Location?> = _locationLiveData

    private val _locationPermissionGranted = MutableLiveData<Boolean>()
    val locationPermissionGranted: LiveData<Boolean> = _locationPermissionGranted

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    private val locationRequest: LocationRequest by lazy {
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .setMaxUpdateDelayMillis(15000)
            .build()
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                _locationLiveData.postValue(location)
            }
        }
    }

    /**
     * Initialise le statut de la permission
     */
    fun setPermissionStatus(isGranted: Boolean) {
        _locationPermissionGranted.value = isGranted
    }

    /**
     * Démarre les mises à jour de localisation
     */
    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        if (_locationPermissionGranted.value == true) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    /**
     * Arrête les mises à jour de localisation
     */
    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    /**
     * Obtient la dernière position connue
     */
    @SuppressLint("MissingPermission")
    fun getLastLocation() {
        if (_locationPermissionGranted.value == true) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    _locationLiveData.value = location
                }
        }
    }
}