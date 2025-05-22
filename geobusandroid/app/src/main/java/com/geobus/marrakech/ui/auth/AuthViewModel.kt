package com.geobus.marrakech.ui.auth

import User
import com.geobus.marrakech.model.AuthResponse
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.geobus.marrakech.repository.AuthRepository
import kotlinx.coroutines.launch

/**
 * ViewModel pour gérer l'authentification des utilisateurs - AMÉLIORÉ
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

    // Type d'erreur pour un traitement spécifique si nécessaire
    private val _errorType = MutableLiveData<String?>()
    val errorType: LiveData<String?> = _errorType

    // Nombre de tentatives de connexion échouées
    private var failedLoginAttempts = 0
    private val maxFailedAttempts = 3

    init {
        // Vérifier si un utilisateur est déjà connecté
        _currentUser.value = repository.getLoggedInUser()
    }

    /**
     * Enregistre un nouvel utilisateur avec gestion d'erreurs améliorée
     */
    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _errorType.value = null

            try {
                val response = repository.register(username, email, password)

                if (response.success && response.username != null && response.email != null) {
                    // Succès de l'enregistrement
                    val user = User(
                        id = response.userId,
                        username = response.username,
                        email = response.email,
                        token = response.token
                    )

                    repository.saveLoggedInUser(user)
                    _currentUser.value = user

                    // Réinitialiser le compteur d'échecs
                    failedLoginAttempts = 0
                } else {
                    // Échec de l'enregistrement
                    _errorMessage.value = response.message ?: "Erreur lors de l'enregistrement"
                    _errorType.value = response.errorCode
                }
            } catch (e: Exception) {
                _errorMessage.value = "Une erreur inattendue est survenue: ${e.message}"
                _errorType.value = "UNKNOWN_ERROR"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Connecte un utilisateur existant avec gestion d'erreurs améliorée
     */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _errorType.value = null

            try {
                val response = repository.login(username, password)

                if (response.success && response.username != null && response.email != null) {
                    // Succès de la connexion
                    val user = User(
                        id = response.userId,
                        username = response.username,
                        email = response.email,
                        token = response.token
                    )

                    repository.saveLoggedInUser(user)
                    _currentUser.value = user

                    // Réinitialiser le compteur d'échecs
                    failedLoginAttempts = 0
                } else {
                    // Échec de la connexion
                    failedLoginAttempts++

                    // Message d'erreur personnalisé selon le nombre de tentatives
                    val errorMessage = when {
                        failedLoginAttempts >= maxFailedAttempts -> {
                            "Trop de tentatives échouées. Veuillez réessayer plus tard ou vérifier vos identifiants."
                        }
                        response.errorCode == AuthResponse.ERROR_INVALID_CREDENTIALS -> {
                            "Nom d'utilisateur ou mot de passe incorrect. Tentative $failedLoginAttempts/$maxFailedAttempts"
                        }
                        else -> {
                            response.message ?: "Identifiants invalides"
                        }
                    }

                    _errorMessage.value = errorMessage
                    _errorType.value = response.errorCode
                }
            } catch (e: Exception) {
                failedLoginAttempts++
                _errorMessage.value = "Une erreur inattendue est survenue: ${e.message}"
                _errorType.value = "UNKNOWN_ERROR"
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
        failedLoginAttempts = 0 // Réinitialiser le compteur
    }

    /**
     * Réinitialise les erreurs
     */
    fun clearError() {
        _errorMessage.value = null
        _errorType.value = null
    }

    /**
     * Réinitialise le compteur de tentatives échouées
     */
    fun resetFailedAttempts() {
        failedLoginAttempts = 0
    }

    /**
     * Retourne le nombre de tentatives échouées
     */
    fun getFailedAttempts(): Int = failedLoginAttempts
}