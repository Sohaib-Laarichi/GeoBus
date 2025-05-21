package com.geobus.marrakech.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.geobus.marrakech.dto.StopDTO;
import com.geobus.marrakech.dto.TimeEstimationDTO;
import com.geobus.marrakech.service.StopService;

/**
 * Contrôleur REST pour gérer les requêtes liées aux stations de bus
 */
@RestController
@RequestMapping("/stops")
public class StopController {

    @Autowired
    private StopService stopService;

    /**
     * Récupère toutes les stations de bus à Marrakech
     *
     * @return Liste des stations
     */
    @GetMapping("/marrakech")
    public ResponseEntity<List<StopDTO>> getAllStopsInMarrakech() {
        List<StopDTO> stops = stopService.getAllStopsInMarrakech();
        return ResponseEntity.ok(stops);
    }

    /**
     * Trouve la station la plus proche des coordonnées fournies
     *
     * @param lat Latitude de l'utilisateur
     * @param lon Longitude de l'utilisateur
     * @return La station la plus proche
     */
    @GetMapping("/nearest")
    public ResponseEntity<StopDTO> getNearestStop(
            @RequestParam("lat") Double lat,
            @RequestParam("lon") Double lon) {

        StopDTO nearestStop = stopService.getNearestStop(lat, lon);
        if (nearestStop == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(nearestStop);
    }

    /**
     * Calcule le temps estimé pour atteindre une station spécifique
     *
     * @param userLat Latitude de l'utilisateur
     * @param userLon Longitude de l'utilisateur
     * @param stopId Identifiant de la station
     * @return Informations sur le temps de trajet
     */
    @GetMapping("/time")
    public ResponseEntity<TimeEstimationDTO> getTimeToStop(
            @RequestParam("userLat") Double userLat,
            @RequestParam("userLon") Double userLon,
            @RequestParam("stopId") Long stopId) {

        TimeEstimationDTO timeEstimation = stopService.getTimeToStop(userLat, userLon, stopId);
        return ResponseEntity.ok(timeEstimation);
    }
}