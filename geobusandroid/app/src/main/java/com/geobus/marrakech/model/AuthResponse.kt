package com.geobus.marrakech.model

/**
 * Réponse d'authentification du serveur
 */
data class AuthResponse(
    val success: Boolean,
    val message: String? = null,
    val user: User? = null
) {
    companion object {
        /**
         * Crée une réponse de succès avec les informations de l'utilisateur
         */
        fun success(id: Long, name: String, email: String): AuthResponse {
            return AuthResponse(
                success = true,
                user = User(id, name, email)
            )
        }

        /**
         * Crée une réponse d'erreur avec un message
         */
        fun error(message: String): AuthResponse {
            return AuthResponse(
                success = false,
                message = message
            )
        }
    }
}