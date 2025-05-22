package com.geobus.marrakech.model

/**
 * Réponse d'authentification du serveur - AMÉLIORÉE
 */
data class AuthResponse(
    val userId: Long?,
    val username: String?,
    val email: String?,
    val token: String?,
    val success: Boolean,
    val message: String?,
    val errorCode: String? = null  // Nouveau: code d'erreur spécifique
) {
    companion object {
        // Codes d'erreur spécifiques
        const val ERROR_INVALID_CREDENTIALS = "INVALID_CREDENTIALS"
        const val ERROR_USER_NOT_FOUND = "USER_NOT_FOUND"
        const val ERROR_WRONG_PASSWORD = "WRONG_PASSWORD"
        const val ERROR_USER_EXISTS = "USER_EXISTS"
        const val ERROR_NETWORK = "NETWORK_ERROR"
        const val ERROR_SERVER = "SERVER_ERROR"

        fun success(userId: Long, username: String, email: String, token: String): AuthResponse {
            return AuthResponse(
                userId = userId,
                username = username,
                email = email,
                token = token,
                success = true,
                message = "Authentification réussie"
            )
        }

        fun error(message: String, errorCode: String? = null): AuthResponse {
            return AuthResponse(
                userId = null,
                username = null,
                email = null,
                token = null,
                success = false,
                message = message,
                errorCode = errorCode
            )
        }

        // Erreurs spécifiques pour l'authentification
        fun invalidCredentials(): AuthResponse {
            return error(
                message = "Nom d'utilisateur ou mot de passe incorrect",
                errorCode = ERROR_INVALID_CREDENTIALS
            )
        }

        fun userNotFound(): AuthResponse {
            return error(
                message = "Aucun compte trouvé avec ce nom d'utilisateur",
                errorCode = ERROR_USER_NOT_FOUND
            )
        }

        fun wrongPassword(): AuthResponse {
            return error(
                message = "Mot de passe incorrect",
                errorCode = ERROR_WRONG_PASSWORD
            )
        }

        fun userAlreadyExists(): AuthResponse {
            return error(
                message = "Un compte existe déjà avec ce nom d'utilisateur ou cette adresse email",
                errorCode = ERROR_USER_EXISTS
            )
        }

        fun networkError(): AuthResponse {
            return error(
                message = "Erreur de connexion. Vérifiez votre connexion internet.",
                errorCode = ERROR_NETWORK
            )
        }

        fun serverError(): AuthResponse {
            return error(
                message = "Erreur serveur. Veuillez réessayer plus tard.",
                errorCode = ERROR_SERVER
            )
        }
    }
}