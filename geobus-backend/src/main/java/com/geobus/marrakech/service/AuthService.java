package com.geobus.marrakech.service;

import com.geobus.marrakech.dto.AuthResponse;
import com.geobus.marrakech.dto.LoginRequest;
import com.geobus.marrakech.dto.RegisterRequest;

/**
 * Interface pour les services d'authentification
 */
public interface AuthService {

    /**
     * Enregistre un nouvel utilisateur
     *
     * @param request Données d'enregistrement
     * @return Réponse d'authentification
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Connecte un utilisateur existant
     *
     * @param request Données de connexion
     * @return Réponse d'authentification
     */
    AuthResponse login(LoginRequest request);
}