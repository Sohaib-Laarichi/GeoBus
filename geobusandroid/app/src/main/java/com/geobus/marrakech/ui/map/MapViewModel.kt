package com.geobus.marrakech.ui.map

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.geobus.marrakech.model.Stop
import com.geobus.marrakech.repository.StopRepository
import kotlinx.coroutines.launch

/**
 * ViewModel pour le fragment de carte
 */
class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StopRepository()

    // Liste des stations de bus
    private val _stops = MutableLiveData<List<Stop>>()
    val stops: LiveData<List<Stop>> = _stops

    // Station la plus proche
    private val _nearestStop = MutableLiveData<Stop?>()
    val nearestStop: LiveData<Stop?> = _nearestStop

    // État de chargement
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Message d'erreur
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    /**
     * Charge toutes les stations de bus de Marrakech
     */
    fun loadStops() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val stopsResult = repository.getAllStopsInMarrakech()
                if (stopsResult != null) {
                    _stops.value = stopsResult
                } else {
                    _errorMessage.value = "Erreur lors du chargement des stations de bus"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Une erreur est survenue"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Trouve la station la plus proche de la position actuelle
     */
    fun findNearestStop(location: Location) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val nearestStop = repository.getNearestStop(location.latitude, location.longitude)
                if (nearestStop != null) {
                    _nearestStop.value = nearestStop
                } else {
                    _errorMessage.value = "Impossible de trouver la station la plus proche"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Une erreur est survenue"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Calcule la station la plus proche en local
     * Utile si l'API ne fonctionne pas ou pour économiser des appels réseau
     */
    fun findNearestStopLocally(location: Location) {
        val currentStops = _stops.value ?: return

        if (currentStops.isEmpty()) {
            _errorMessage.value = "Aucune station disponible"
            return
        }

        val userLatitude = location.latitude
        val userLongitude = location.longitude

        // Trouver la station la plus proche
        val nearest = currentStops.minByOrNull { stop ->
            stop.distanceTo(userLatitude, userLongitude)
        }

        // Mettre à jour la distance et le temps de marche estimé
        nearest?.let {
            val distance = it.distanceTo(userLatitude, userLongitude)
            val walkingTime = it.estimateWalkingTime(userLatitude, userLongitude)

            val updatedStop = it.copy(
                distance = distance,
                walkingTimeMinutes = walkingTime
            )

            _nearestStop.value = updatedStop
        }
    }

    /**
     * Réinitialise les erreurs
     */
    fun clearError() {
        _errorMessage.value = null
    }
}