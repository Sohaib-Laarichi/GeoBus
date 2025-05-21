package com.geobus.marrakech.service;

import java.util.List;

import com.geobus.marrakech.dto.StopDTO;
import com.geobus.marrakech.dto.TimeEstimationDTO;
import com.geobus.marrakech.model.Stop;

/**
 * Interface pour les services liés aux stations de bus
 */
public interface StopService {

    /**
     * Récupère toutes les stations de bus à Marrakech
     *
     * @return Liste des stations
     */
    List<StopDTO> getAllStopsInMarrakech();

    /**
     * Trouve la station la plus proche des coordonnées de l'utilisateur
     *
     * @param lat Latitude de l'utilisateur
     * @param lon Longitude de l'utilisateur
     * @return La station la plus proche avec distance et temps estimé
     */
    StopDTO getNearestStop(Double lat, Double lon);

    /**
     * Calcule le temps estimé pour atteindre une station spécifique
     *
     * @param userLat Latitude de l'utilisateur
     * @param userLon Longitude de l'utilisateur
     * @param stopId Identifiant de la station
     * @return Informations sur le temps de trajet
     */
    TimeEstimationDTO getTimeToStop(Double userLat, Double userLon, Long stopId);
}