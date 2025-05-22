/**
 * Modèle utilisateur corrigé
 */
data class User(
    val id: Long? = null,
    val username: String,  // Changé de 'name' à 'username'
    val email: String,
    val token: String? = null
)