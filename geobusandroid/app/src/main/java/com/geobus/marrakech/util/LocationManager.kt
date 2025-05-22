package com.geobus.marrakech.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*

/**
 * Gestionnaire de localisation pour l'application
 */
class LocationManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _locationLiveData = MutableLiveData<Location>()
    val locationLiveData: LiveData<Location> = _locationLiveData

    private val _locationPermissionGranted = MutableLiveData<Boolean>()
    val locationPermissionGranted: LiveData<Boolean> = _locationPermissionGranted

    private val locationRequest = LocationRequest.create().apply {
        interval = 10000 // 10 secondes
        fastestInterval = 5000 // 5 secondes
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                _locationLiveData.value = location
            }
        }
    }

    fun setPermissionStatus(granted: Boolean) {
        _locationPermissionGranted.value = granted
    }

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

    fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    fun getLastLocation() {
        if (_locationPermissionGranted.value == true) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    _locationLiveData.value = it
                }
            }
        }
    }
}