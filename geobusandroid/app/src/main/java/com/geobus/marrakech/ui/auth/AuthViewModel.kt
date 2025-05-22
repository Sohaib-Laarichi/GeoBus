package com.geobus.marrakech.ui.auth

import User
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope

import com.geobus.marrakech.repository.AuthRepository
import kotlinx.coroutines.launch

/**
 * ViewModel pour gérer l'authentification des utilisateurs - CORRIGÉ
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
     * Enregistre un nouvel utilisateur - CORRIGÉ
     *
     * @param username Nom d'utilisateur (changé de name à username)
     * @param email Email de l'utilisateur
     * @param password Mot de passe de l'utilisateur
     */
    fun register(username: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = repository.register(username, email, password)

                if (response.success && response.username != null && response.email != null) {
                    // CORRECTION: Créer l'utilisateur avec les bonnes données
                    val user = User(
                        id = response.userId,
                        username = response.username,
                        email = response.email,
                        token = response.token
                    )

                    // Sauvegarder l'utilisateur connecté
                    repository.saveLoggedInUser(user)
                    _currentUser.value = user
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
     * Connecte un utilisateur existant - CORRIGÉ
     *
     * @param username Nom d'utilisateur (changé d'email à username)
     * @param password Mot de passe de l'utilisateur
     */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            try {
                val response = repository.login(username, password)

                if (response.success && response.username != null && response.email != null) {
                    // CORRECTION: Créer l'utilisateur avec les bonnes données
                    val user = User(
                        id = response.userId,
                        username = response.username,
                        email = response.email,
                        token = response.token
                    )

                    // Sauvegarder l'utilisateur connecté
                    repository.saveLoggedInUser(user)
                    _currentUser.value = user
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