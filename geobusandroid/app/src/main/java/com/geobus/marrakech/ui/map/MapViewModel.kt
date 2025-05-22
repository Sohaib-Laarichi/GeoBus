package com.geobus.marrakech.ui.map

import android.app.Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.geobus.marrakech.model.BusPosition
import com.geobus.marrakech.model.Stop
import com.geobus.marrakech.repository.BusPositionRepository
import com.geobus.marrakech.repository.StopRepository
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule

/**
 * ViewModel pour le fragment de carte avec gestion des bus
 */
class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val stopRepository = StopRepository()
    private val busRepository = BusPositionRepository()

    // Liste des stations de bus
    private val _stops = MutableLiveData<List<Stop>>()
    val stops: LiveData<List<Stop>> = _stops

    // Station la plus proche
    private val _nearestStop = MutableLiveData<Stop?>()
    val nearestStop: LiveData<Stop?> = _nearestStop

    // Positions des bus
    private val _busPositions = MutableLiveData<List<BusPosition>>()
    val busPositions: LiveData<List<BusPosition>> = _busPositions

    // Bus pour une station spécifique
    private val _busesForStop = MutableLiveData<List<BusPosition>>()
    val busesForStop: LiveData<List<BusPosition>> = _busesForStop

    // État de chargement
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Message d'erreur
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Contrôle du rafraîchissement automatique
    private var refreshTimer: Timer? = null
    private var isRefreshingEnabled = false

    /**
     * Charge toutes les stations de bus de Marrakech
     */
    fun loadStops() {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val stopsResult = stopRepository.getAllStopsInMarrakech()
                if (stopsResult != null) {
                    _stops.value = stopsResult
                    Log.d("MapViewModel", "Stations chargées: ${stopsResult.size}")
                } else {
                    _errorMessage.value = "Erreur lors du chargement des stations de bus"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Une erreur est survenue"
                Log.e("MapViewModel", "Erreur chargement stations: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Charge toutes les positions des bus
     */
    fun loadBusPositions() {
        viewModelScope.launch {
            try {
                val busResult = busRepository.getAllLatestPositions()
                if (busResult != null) {
                    _busPositions.value = busResult
                    Log.d("MapViewModel", "Positions bus chargées: ${busResult.size}")
                } else {
                    Log.w("MapViewModel", "Aucune position de bus récupérée")
                }
            } catch (e: Exception) {
                Log.e("MapViewModel", "Erreur chargement positions bus: ${e.message}")
            }
        }
    }

    /**
     * Charge les bus qui se dirigent vers une station spécifique
     */
    fun loadBusesForStop(stopId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val busesResult = busRepository.getBusesGoingToStop(stopId)
                if (busesResult != null) {
                    _busesForStop.value = busesResult
                    Log.d("MapViewModel", "Bus pour station $stopId: ${busesResult.size}")
                } else {
                    _errorMessage.value = "Aucun bus trouvé pour cette station"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Erreur lors du chargement des bus"
                Log.e("MapViewModel", "Erreur chargement bus pour station: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Démarre le rafraîchissement automatique des positions des bus toutes les 10 secondes
     */
    fun startPeriodicRefresh() {
        if (isRefreshingEnabled) return

        isRefreshingEnabled = true
        refreshTimer = Timer()
        refreshTimer?.schedule(0, 10000) { // Démarrer immédiatement, puis toutes les 10 secondes
            if (isRefreshingEnabled) {
                loadBusPositions()
            }
        }
        Log.d("MapViewModel", "Rafraîchissement automatique démarré")
    }

    /**
     * Arrête le rafraîchissement automatique
     */
    fun stopPeriodicRefresh() {
        isRefreshingEnabled = false
        refreshTimer?.cancel()
        refreshTimer = null
        Log.d("MapViewModel", "Rafraîchissement automatique arrêté")
    }

    /**
     * Trouve la station la plus proche de la position actuelle
     */
    fun findNearestStop(location: Location) {
        viewModelScope.launch {
            try {
                val nearestStop = stopRepository.getNearestStop(location.latitude, location.longitude)
                if (nearestStop != null) {
                    _nearestStop.value = nearestStop
                } else {
                    // Fallback vers calcul local
                    findNearestStopLocally(location)
                }
            } catch (e: Exception) {
                Log.e("MapViewModel", "Erreur API station proche, utilisation calcul local: ${e.message}")
                // Fallback vers calcul local en cas d'erreur API
                findNearestStopLocally(location)
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
            Log.d("MapViewModel", "Station la plus proche: ${it.stopName}, distance: ${distance.toInt()}m")
        }
    }

    /**
     * Réinitialise les erreurs
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Nettoyage quand le ViewModel est détruit
     */
    override fun onCleared() {
        super.onCleared()
        stopPeriodicRefresh()
    }
}