/**
 * Réponse d'authentification du serveur
 * CORRECTION: Structure mise à jour pour correspondre au backend
 */
data class AuthResponse(
    val userId: Long?,
    val username: String?,
    val email: String?,
    val token: String?,
    val success: Boolean,
    val message: String?
) {
    companion object {
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

        fun error(message: String): AuthResponse {
            return AuthResponse(
                userId = null,
                username = null,
                email = null,
                token = null,
                success = false,
                message = message
            )
        }
    }
}