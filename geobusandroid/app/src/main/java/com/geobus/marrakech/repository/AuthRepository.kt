package com.geobus.marrakech.repository

import com.geobus.marrakech.model.AuthResponse
import com.geobus.marrakech.model.LoginRequest
import com.geobus.marrakech.model.RegisterRequest
import com.geobus.marrakech.model.User
import com.geobus.marrakech.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository pour gérer l'authentification des utilisateurs
 */
class AuthRepository {
    
    private val apiClient = ApiClient.authService
    
    /**
     * Enregistre un nouvel utilisateur
     * 
     * @param name Nom de l'utilisateur
     * @param email Email de l'utilisateur
     * @param password Mot de passe de l'utilisateur
     * @return Réponse d'authentification
     */
    suspend fun register(name: String, email: String, password: String): AuthResponse {
        return withContext(Dispatchers.IO) {
            try {
                val request = RegisterRequest(name, email, password)
                val response = apiClient.register(request)
                response
            } catch (e: Exception) {
                AuthResponse.error(e.message ?: "Erreur lors de l'enregistrement")
            }
        }
    }
    
    /**
     * Connecte un utilisateur existant
     * 
     * @param email Email de l'utilisateur
     * @param password Mot de passe de l'utilisateur
     * @return Réponse d'authentification
     */
    suspend fun login(email: String, password: String): AuthResponse {
        return withContext(Dispatchers.IO) {
            try {
                val request = LoginRequest(email, password)
                val response = apiClient.login(request)
                response
            } catch (e: Exception) {
                AuthResponse.error(e.message ?: "Erreur lors de la connexion")
            }
        }
    }
    
    /**
     * Sauvegarde les informations de l'utilisateur connecté
     * 
     * @param user Utilisateur à sauvegarder
     */
    fun saveLoggedInUser(user: User) {
        // TODO: Implémenter la sauvegarde des informations de l'utilisateur
        // Utiliser SharedPreferences ou DataStore
    }
    
    /**
     * Récupère l'utilisateur connecté
     * 
     * @return Utilisateur connecté ou null si aucun utilisateur n'est connecté
     */
    fun getLoggedInUser(): User? {
        // TODO: Implémenter la récupération des informations de l'utilisateur
        // Utiliser SharedPreferences ou DataStore
        return null
    }
    
    /**
     * Déconnecte l'utilisateur
     */
    fun logout() {
        // TODO: Implémenter la déconnexion de l'utilisateur
        // Effacer les informations de l'utilisateur des SharedPreferences ou DataStore
    }
}