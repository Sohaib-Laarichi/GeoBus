package com.geobus.marrakech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour les réponses d'authentification
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private Long userId;
    private String username;
    private String email;
    private String token;
    private boolean success;
    private String message;
    
    /**
     * Constructeur pour une réponse réussie
     */
    public static AuthResponse success(Long userId, String username, String email, String token) {
        return new AuthResponse(userId, username, email, token, true, "Authentification réussie");
    }
    
    /**
     * Constructeur pour une réponse d'erreur
     */
    public static AuthResponse error(String message) {
        return new AuthResponse(null, null, null, null, false, message);
    }
}