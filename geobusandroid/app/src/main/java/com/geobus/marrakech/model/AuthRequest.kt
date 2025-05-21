package com.geobus.marrakech.model

/**
 * Classe de base pour les requêtes d'authentification
 */
sealed class AuthRequest

/**
 * Requête pour l'enregistrement d'un nouvel utilisateur
 */
data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
) : AuthRequest()

/**
 * Requête pour la connexion d'un utilisateur existant
 */
data class LoginRequest(
    val email: String,
    val password: String
) : AuthRequest()