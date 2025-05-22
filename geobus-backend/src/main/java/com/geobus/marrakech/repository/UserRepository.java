package com.geobus.marrakech.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.geobus.marrakech.model.User;

/**
 * Repository pour accéder aux données des utilisateurs
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Trouve un utilisateur par son nom d'utilisateur
     *
     * @param username Nom d'utilisateur
     * @return Utilisateur trouvé ou vide
     */
    Optional<User> findByUsername(String username);

    /**
     * Trouve un utilisateur par son email
     *
     * @param email Email de l'utilisateur
     * @return Utilisateur trouvé ou vide
     */
    Optional<User> findByEmail(String email);

    /**
     * Vérifie si un nom d'utilisateur existe déjà
     *
     * @param username Nom d'utilisateur
     * @return true si le nom d'utilisateur existe, false sinon
     */
    boolean existsByUsername(String username);

    /**
     * Vérifie si un email existe déjà
     *
     * @param email Email
     * @return true si l'email existe, false sinon
     */
    boolean existsByEmail(String email);
}