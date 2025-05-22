package com.geobus.marrakech.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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
@CrossOrigin(origins = "*", maxAge = 3600)
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
        try {
            List<StopDTO> stops = stopService.getAllStopsInMarrakech();
            return ResponseEntity.ok(stops);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
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

        try {
            StopDTO nearestStop = stopService.getNearestStop(lat, lon);
            if (nearestStop == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(nearestStop);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
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

        try {
            TimeEstimationDTO timeEstimation = stopService.getTimeToStop(userLat, userLon, stopId);
            if (timeEstimation == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(timeEstimation);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint simple pour tester l'accès aux stations
     */
    @GetMapping("/test")
    public ResponseEntity<String> testStopsEndpoint() {
        return ResponseEntity.ok("Endpoint des stations accessible !");
    }
}