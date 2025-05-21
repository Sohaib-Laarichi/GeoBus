package com.geobus.marrakech.network

import com.geobus.marrakech.model.AuthResponse
import com.geobus.marrakech.model.LoginRequest
import com.geobus.marrakech.model.RegisterRequest
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Interface pour les requêtes d'authentification
 */
interface AuthService {
    
    /**
     * Enregistre un nouvel utilisateur
     * 
     * @param request Données d'enregistrement
     * @return Réponse d'authentification
     */
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse
    
    /**
     * Connecte un utilisateur existant
     * 
     * @param request Données de connexion
     * @return Réponse d'authentification
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse
}