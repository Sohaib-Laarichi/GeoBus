package com.geobus.marrakech.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.geobus.marrakech.dto.AuthResponse;
import com.geobus.marrakech.dto.LoginRequest;
import com.geobus.marrakech.dto.RegisterRequest;
import com.geobus.marrakech.model.User;
import com.geobus.marrakech.repository.UserRepository;
import com.geobus.marrakech.security.JwtUtils;

/**
 * Implémentation des services d'authentification
 */
@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse register(RegisterRequest request) {
        try {
            logger.info("Début de l'enregistrement pour l'utilisateur: {}", request.getUsername());

            // Vérifier si le nom d'utilisateur existe déjà
            if (userRepository.existsByUsername(request.getUsername())) {
                logger.warn("Nom d'utilisateur déjà existant: {}", request.getUsername());
                return AuthResponse.error("Ce nom d'utilisateur est déjà utilisé");
            }

            // Vérifier si l'email existe déjà
            if (userRepository.existsByEmail(request.getEmail())) {
                logger.warn("Email déjà existant: {}", request.getEmail());
                return AuthResponse.error("Cet email est déjà utilisé");
            }

            // Créer un nouvel utilisateur
            User user = new User();
            user.setUsername(request.getUsername());
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setCreatedAt(LocalDateTime.now());

            logger.info("Tentative de sauvegarde de l'utilisateur: {}", user.getUsername());

            // Sauvegarder l'utilisateur
            user = userRepository.save(user);

            logger.info("Utilisateur sauvegardé avec ID: {}", user.getId());

            // Générer un JWT token
            String token = jwtUtils.generateJwtToken(user.getUsername());

            logger.info("Token JWT généré pour l'utilisateur: {}", user.getUsername());

            return AuthResponse.success(user.getId(), user.getUsername(), user.getEmail(), token);

        } catch (Exception e) {
            logger.error("Erreur lors de l'enregistrement de l'utilisateur: {}", request.getUsername(), e);
            return AuthResponse.error("Erreur lors de l'enregistrement: " + e.getMessage());
        }
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            logger.info("Tentative de connexion pour l'utilisateur: {}", request.getUsername());

            // Trouver l'utilisateur par son nom d'utilisateur
            Optional<User> userOpt = userRepository.findByUsername(request.getUsername());

            if (userOpt.isEmpty()) {
                logger.warn("Utilisateur non trouvé: {}", request.getUsername());
                return AuthResponse.error("Nom d'utilisateur ou mot de passe incorrect");
            }

            User user = userOpt.get();

            // Vérifier le mot de passe avec BCrypt
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                logger.warn("Mot de passe incorrect pour l'utilisateur: {}", request.getUsername());
                return AuthResponse.error("Nom d'utilisateur ou mot de passe incorrect");
            }

            // Générer un JWT token
            String token = jwtUtils.generateJwtToken(user.getUsername());

            logger.info("Connexion réussie pour l'utilisateur: {}", user.getUsername());

            return AuthResponse.success(user.getId(), user.getUsername(), user.getEmail(), token);

        } catch (Exception e) {
            logger.error("Erreur lors de la connexion de l'utilisateur: {}", request.getUsername(), e);
            return AuthResponse.error("Erreur lors de la connexion: " + e.getMessage());
        }
    }
}