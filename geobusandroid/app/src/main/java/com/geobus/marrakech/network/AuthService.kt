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

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse
}