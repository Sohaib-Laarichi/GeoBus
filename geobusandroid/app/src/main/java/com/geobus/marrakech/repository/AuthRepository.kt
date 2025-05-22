package com.geobus.marrakech.repository

import AuthResponse
import User

import com.geobus.marrakech.model.LoginRequest
import com.geobus.marrakech.model.RegisterRequest

import com.geobus.marrakech.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository pour gérer l'authentification des utilisateurs - CORRIGÉ
 */
class AuthRepository {

    private val apiClient = ApiClient.authService

    /**
     * Enregistre un nouvel utilisateur - CORRIGÉ
     *
     * @param username Nom d'utilisateur (changé de name à username)
     * @param email Email de l'utilisateur
     * @param password Mot de passe de l'utilisateur
     * @return Réponse d'authentification
     */
    suspend fun register(username: String, email: String, password: String): AuthResponse {
        return withContext(Dispatchers.IO) {
            try {
                val request = RegisterRequest(username, email, password)
                val response = apiClient.register(request)
                response
            } catch (e: Exception) {
                AuthResponse.error(e.message ?: "Erreur lors de l'enregistrement")
            }
        }
    }

    /**
     * Connecte un utilisateur existant - CORRIGÉ
     *
     * @param username Nom d'utilisateur (changé d'email à username)
     * @param password Mot de passe de l'utilisateur
     * @return Réponse d'authentification
     */
    suspend fun login(username: String, password: String): AuthResponse {
        return withContext(Dispatchers.IO) {
            try {
                val request = LoginRequest(username, password)
                val response = apiClient.login(request)
                response
            } catch (e: Exception) {
                AuthResponse.error(e.message ?: "Erreur lors de la connexion")
            }
        }
    }

    /**
     * Sauvegarde les informations de l'utilisateur connecté
     * TODO: Implémenter avec SharedPreferences ou DataStore
     *
     * @param user Utilisateur à sauvegarder
     */
    fun saveLoggedInUser(user: User) {
        // TODO: Implémenter la sauvegarde des informations de l'utilisateur
        // Utiliser SharedPreferences ou DataStore
        println("Sauvegarde utilisateur: ${user.username}")
    }

    /**
     * Récupère l'utilisateur connecté
     * TODO: Implémenter avec SharedPreferences ou DataStore
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
     * TODO: Implémenter avec SharedPreferences ou DataStore
     */
    fun logout() {
        // TODO: Implémenter la déconnexion de l'utilisateur
        // Effacer les informations de l'utilisateur des SharedPreferences ou DataStore
        println("Déconnexion utilisateur")
    }
}