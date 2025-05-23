package com.geobus.marrakech.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.geobus.marrakech.model.Stop;

/**
 * Repository pour accéder aux données des stations de bus
 */
@Repository
public interface StopRepository extends JpaRepository<Stop, Long> {

    /**
     * Trouve toutes les stations dans une ville spécifique
     *
     * @param ville Nom de la ville
     * @return Liste des stations dans cette ville
     */
    List<Stop> findByVilleIgnoreCase(String ville);

    /**
     * Trouve la station la plus proche des coordonnées données
     * Utilise une requête native SQL avec le calcul de Haversine
     *
     * @param lat Latitude de l'utilisateur
     * @param lon Longitude de l'utilisateur
     * @return La station la plus proche
     */
    @Query(value = "SELECT * FROM stops ORDER BY " +
            "(6371 * acos(cos(radians(?1)) * cos(radians(latitude)) * cos(radians(longitude) - radians(?2)) + sin(radians(?1)) * sin(radians(latitude)))) ASC LIMIT 1",
            nativeQuery = true)
    Stop findNearestStop(Double lat, Double lon);

    /**
     * Trouve les stations dans un rayon donné
     */
    @Query("SELECT s FROM Stop s WHERE " +
            "(6371 * acos(cos(radians(:lat)) * cos(radians(s.latitude)) * " +
            "cos(radians(s.longitude) - radians(:lng)) + " +
            "sin(radians(:lat)) * sin(radians(s.latitude)))) < :radius")
    List<Stop> findStopsWithinRadius(@Param("lat") Double latitude,
                                     @Param("lng") Double longitude,
                                     @Param("radius") Double radiusKm);

    /**
     * Recherche par nom de station
     */
    List<Stop> findByStopNameContainingIgnoreCase(String name);
}