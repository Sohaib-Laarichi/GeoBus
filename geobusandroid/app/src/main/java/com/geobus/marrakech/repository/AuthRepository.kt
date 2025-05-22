package com.geobus.marrakech.repository

import com.geobus.marrakech.model.AuthResponse
import User
import com.geobus.marrakech.model.LoginRequest
import com.geobus.marrakech.model.RegisterRequest
import com.geobus.marrakech.network.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Repository pour gérer l'authentification des utilisateurs - AMÉLIORÉ
 */
class AuthRepository {

    private val apiClient = ApiClient.authService

    /**
     * Enregistre un nouvel utilisateur avec gestion d'erreurs améliorée
     */
    suspend fun register(username: String, email: String, password: String): AuthResponse {
        return withContext(Dispatchers.IO) {
            try {
                val request = RegisterRequest(username, email, password)
                val response = apiClient.register(request)

                // Vérifier si la réponse du serveur indique un succès
                if (response.success) {
                    response
                } else {
                    // Le serveur a retourné une erreur métier
                    handleServerError(response)
                }
            } catch (e: HttpException) {
                // Erreurs HTTP (4xx, 5xx)
                handleHttpError(e, "register")
            } catch (e: ConnectException) {
                AuthResponse.networkError()
            } catch (e: SocketTimeoutException) {
                AuthResponse.error("Timeout de connexion. Veuillez réessayer.", AuthResponse.ERROR_NETWORK)
            } catch (e: UnknownHostException) {
                AuthResponse.error("Impossible de joindre le serveur. Vérifiez votre connexion.", AuthResponse.ERROR_NETWORK)
            } catch (e: Exception) {
                AuthResponse.error("Erreur lors de l'enregistrement: ${e.message}", AuthResponse.ERROR_SERVER)
            }
        }
    }

    /**
     * Connecte un utilisateur existant avec gestion d'erreurs améliorée
     */
    suspend fun login(username: String, password: String): AuthResponse {
        return withContext(Dispatchers.IO) {
            try {
                val request = LoginRequest(username, password)
                val response = apiClient.login(request)

                // Vérifier si la réponse du serveur indique un succès
                if (response.success) {
                    response
                } else {
                    // Le serveur a retourné une erreur métier
                    handleServerError(response)
                }
            } catch (e: HttpException) {
                // Erreurs HTTP (4xx, 5xx)
                handleHttpError(e, "login")
            } catch (e: ConnectException) {
                AuthResponse.networkError()
            } catch (e: SocketTimeoutException) {
                AuthResponse.error("Timeout de connexion. Veuillez réessayer.", AuthResponse.ERROR_NETWORK)
            } catch (e: UnknownHostException) {
                AuthResponse.error("Impossible de joindre le serveur. Vérifiez votre connexion.", AuthResponse.ERROR_NETWORK)
            } catch (e: Exception) {
                AuthResponse.error("Erreur lors de la connexion: ${e.message}", AuthResponse.ERROR_SERVER)
            }
        }
    }

    /**
     * Gère les erreurs HTTP retournées par le serveur
     */
    private fun handleHttpError(exception: HttpException, operation: String): AuthResponse {
        return when (exception.code()) {
            400 -> {
                // Bad Request - données invalides
                if (operation == "login") {
                    AuthResponse.invalidCredentials()
                } else {
                    AuthResponse.error("Données invalides. Vérifiez vos informations.", AuthResponse.ERROR_INVALID_CREDENTIALS)
                }
            }
            401 -> {
                // Unauthorized - identifiants incorrects
                AuthResponse.invalidCredentials()
            }
            404 -> {
                // Not Found - utilisateur n'existe pas
                AuthResponse.userNotFound()
            }
            409 -> {
                // Conflict - utilisateur existe déjà (pour register)
                AuthResponse.userAlreadyExists()
            }
            422 -> {
                // Unprocessable Entity - validation échouée
                AuthResponse.error("Les données fournies ne sont pas valides.", AuthResponse.ERROR_INVALID_CREDENTIALS)
            }
            500 -> {
                // Internal Server Error
                AuthResponse.serverError()
            }
            503 -> {
                // Service Unavailable
                AuthResponse.error("Service temporairement indisponible. Réessayez plus tard.", AuthResponse.ERROR_SERVER)
            }
            else -> {
                AuthResponse.error("Erreur serveur (${exception.code()}). Réessayez plus tard.", AuthResponse.ERROR_SERVER)
            }
        }
    }

    /**
     * Gère les erreurs métier retournées par le serveur
     */
    private fun handleServerError(response: AuthResponse): AuthResponse {
        return when (response.errorCode) {
            AuthResponse.ERROR_INVALID_CREDENTIALS,
            AuthResponse.ERROR_USER_NOT_FOUND,
            AuthResponse.ERROR_WRONG_PASSWORD -> {
                AuthResponse.invalidCredentials()
            }
            AuthResponse.ERROR_USER_EXISTS -> {
                AuthResponse.userAlreadyExists()
            }
            else -> {
                response // Retourner la réponse originale si pas de code d'erreur spécifique
            }
        }
    }

    /**
     * Sauvegarde les informations de l'utilisateur connecté
     */
    fun saveLoggedInUser(user: User) {
        // TODO: Implémenter avec SharedPreferences ou DataStore
        println("Sauvegarde utilisateur: ${user.username}")
    }

    /**
     * Récupère l'utilisateur connecté
     */
    fun getLoggedInUser(): User? {
        // TODO: Implémenter avec SharedPreferences ou DataStore
        return null
    }

    /**
     * Déconnecte l'utilisateur
     */
    fun logout() {
        // TODO: Implémenter avec SharedPreferences ou DataStore
        println("Déconnexion utilisateur")
    }
}