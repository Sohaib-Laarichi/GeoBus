package com.geobus.marrakech.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.geobus.marrakech.model.User
import com.geobus.marrakech.repository.AuthRepository
import kotlinx.coroutines.launch

/**
 * ViewModel pour gérer l'authentification des utilisateurs
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AuthRepository()

    // Utilisateur connecté
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    // État de chargement
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // Message d'erreur
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        // Vérifier si un utilisateur est déjà connecté
        _currentUser.value = repository.getLoggedInUser()
    }

    /**
     * Enregistre un nouvel utilisateur
     * 
     * @param name Nom de l'utilisateur
     * @param email Email de l'utilisateur
     * @param password Mot de passe de l'utilisateur
     */
    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = repository.register(name, email, password)
                
                if (response.success && response.user != null) {
                    // Sauvegarder l'utilisateur connecté
                    repository.saveLoggedInUser(response.user)
                    _currentUser.value = response.user
                } else {
                    _errorMessage.value = response.message ?: "Erreur lors de l'enregistrement"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Une erreur est survenue"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Connecte un utilisateur existant
     * 
     * @param email Email de l'utilisateur
     * @param password Mot de passe de l'utilisateur
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = repository.login(email, password)
                
                if (response.success && response.user != null) {
                    // Sauvegarder l'utilisateur connecté
                    repository.saveLoggedInUser(response.user)
                    _currentUser.value = response.user
                } else {
                    _errorMessage.value = response.message ?: "Identifiants invalides"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Une erreur est survenue"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Déconnecte l'utilisateur
     */
    fun logout() {
        repository.logout()
        _currentUser.value = null
    }

    /**
     * Réinitialise les erreurs
     */
    fun clearError() {
        _errorMessage.value = null
    }
}