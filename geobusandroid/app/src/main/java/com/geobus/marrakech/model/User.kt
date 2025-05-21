package com.geobus.marrakech.model

/**
 * Modèle représentant un utilisateur de l'application
 */
data class User(
    val id: Long? = null,
    val name: String,
    val email: String,
    val password: String? = null
) {
    /**
     * Constructeur secondaire pour créer un utilisateur sans mot de passe
     * Utile pour afficher les informations de l'utilisateur sans exposer le mot de passe
     */
    constructor(id: Long, name: String, email: String) : this(id, name, email, null)
}