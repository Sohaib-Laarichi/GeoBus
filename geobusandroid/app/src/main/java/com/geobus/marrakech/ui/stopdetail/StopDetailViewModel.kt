package com.geobus.marrakech.ui.stopdetail

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.geobus.marrakech.model.Stop
import com.geobus.marrakech.model.TimeEstimation
import com.geobus.marrakech.repository.StopRepository
import kotlinx.coroutines.launch

/**
 * ViewModel pour le fragment de détail d'une station
 */
class StopDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StopRepository()

    // Estimation de temps et distance
    private val _timeEstimation = MutableLiveData<TimeEstimation?>()
    val timeEstimation: LiveData<TimeEstimation?> = _timeEstimation

    // Station sélectionnée
    private val _selectedStop = MutableLiveData<Stop?>()
    val selectedStop: LiveData<Stop?> = _selectedStop

    // État de chargement
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Message d'erreur
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    /**
     * Définit la station sélectionnée
     */
    fun setSelectedStop(stop: Stop) {
        _selectedStop.value = stop
    }

    /**
     * Calcule le temps estimé pour atteindre la station
     */
    fun getTimeToStop(userLocation: Location, stopId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val result = repository.getTimeToStop(
                    userLocation.latitude,
                    userLocation.longitude,
                    stopId
                )

                if (result != null) {
                    _timeEstimation.value = result
                } else {
                    _errorMessage.value = "Impossible de calculer le temps de trajet"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Une erreur est survenue"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Calcule le temps estimé localement
     * Utile si l'API ne fonctionne pas
     */
    fun calculateLocalEstimation(userLocation: Location) {
        val stop = _selectedStop.value ?: return

        val distance = stop.distanceTo(userLocation.latitude, userLocation.longitude)
        val walkingTime = stop.estimateWalkingTime(userLocation.latitude, userLocation.longitude)

        _timeEstimation.value = TimeEstimation(
            stopId = stop.stopId,
            stopName = stop.stopName,
            distance = distance,
            walkingTimeMinutes = walkingTime
        )
    }

    /**
     * Réinitialise les erreurs
     */
    fun clearError() {
        _errorMessage.value = null
    }
}