package com.geobus.marrakech.controller;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.geobus.marrakech.dto.AuthResponse;
import com.geobus.marrakech.dto.LoginRequest;
import com.geobus.marrakech.dto.RegisterRequest;
import com.geobus.marrakech.service.AuthService;

/**
 * Contrôleur pour l'authentification
 */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    /**
     * Enregistrement d'un nouvel utilisateur
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody RegisterRequest request) {
        logger.info("Requête d'enregistrement reçue pour: {}", request.getUsername());

        try {
            AuthResponse response = authService.register(request);

            if (response.isSuccess()) {
                logger.info("Enregistrement réussi pour: {}", request.getUsername());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Échec d'enregistrement pour: {} - Raison: {}", request.getUsername(), response.getMessage());
                return ResponseEntity.badRequest().body(response);
            }
        } catch (Exception e) {
            logger.error("Erreur inattendue lors de l'enregistrement pour: {}", request.getUsername(), e);
            return ResponseEntity.status(500)
                    .body(AuthResponse.error("Erreur interne du serveur: " + e.getMessage()));
        }
    }

    /**
     * Connexion d'un utilisateur
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginUser(@Valid @RequestBody LoginRequest request) {
        logger.info("Requête de connexion reçue pour: {}", request.getUsername());

        try {
            AuthResponse response = authService.login(request);

            if (response.isSuccess()) {
                logger.info("Connexion réussie pour: {}", request.getUsername());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Échec de connexion pour: {} - Raison: {}", request.getUsername(), response.getMessage());
                return ResponseEntity.status(401).body(response);
            }
        } catch (Exception e) {
            logger.error("Erreur inattendue lors de la connexion pour: {}", request.getUsername(), e);
            return ResponseEntity.status(500)
                    .body(AuthResponse.error("Erreur interne du serveur: " + e.getMessage()));
        }
    }
}