package com.geobus.marrakech.model

/**
 * Requête pour l'enregistrement d'un nouvel utilisateur
 * CORRECTION: Utilise 'username' au lieu de 'name' pour correspondre au backend
 */
data class RegisterRequest(
    val username: String,  // Changé de 'name' à 'username'
    val email: String,
    val password: String
)

/**
 * Requête pour la connexion d'un utilisateur existant
 * CORRECTION: Utilise 'username' au lieu de 'email' pour correspondre au backend
 */
data class LoginRequest(
    val username: String,  // Changé de 'email' à 'username'
    val password: String
)