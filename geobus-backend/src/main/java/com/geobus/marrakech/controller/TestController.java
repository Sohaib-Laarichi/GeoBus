package com.geobus.marrakech.controller;

import com.geobus.marrakech.model.Stop;
import com.geobus.marrakech.model.BusPosition;
import com.geobus.marrakech.repository.StopRepository;
import com.geobus.marrakech.repository.BusPositionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Contrôleur de test pour vérifier l'authentification et les endpoints
 */
@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TestController {

    @Autowired
    private StopRepository stopRepository;

    @Autowired
    private BusPositionRepository busPositionRepository;

    /**
     * Endpoint public pour tester la connectivité
     */
    @GetMapping("/public")
    public ResponseEntity<Map<String, String>> publicTest() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Endpoint public accessible");
        response.put("status", "success");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint protégé pour tester l'authentification
     */
    @GetMapping("/protected")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> protectedTest() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Endpoint protégé accessible - Authentification réussie!");
        response.put("status", "success");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint pour obtenir toutes les stations
     */
    @GetMapping("/stops")
    public ResponseEntity<List<Stop>> getAllStops() {
        List<Stop> stops = stopRepository.findAll();
        return ResponseEntity.ok(stops);
    }

    /**
     * Endpoint pour obtenir les stations par ville
     */
    @GetMapping("/stops/city/{ville}")
    public ResponseEntity<List<Stop>> getStopsByCity(@PathVariable String ville) {
        List<Stop> stops = stopRepository.findByVilleIgnoreCase(ville);
        return ResponseEntity.ok(stops);
    }

    /**
     * Endpoint pour obtenir les positions actuelles des bus
     */
    @GetMapping("/buses/positions")
    public ResponseEntity<List<BusPosition>> getAllBusPositions() {
        List<BusPosition> positions = busPositionRepository.findAll();
        return ResponseEntity.ok(positions);
    }

    /**
     * Endpoint pour vérifier le statut de la base de données
     */
    @GetMapping("/db/status")
    public ResponseEntity<Map<String, Object>> getDatabaseStatus() {
        Map<String, Object> response = new HashMap<>();
        try {
            long stopsCount = stopRepository.count();
            long busPositionsCount = busPositionRepository.count();

            response.put("status", "connected");
            response.put("stopsCount", stopsCount);
            response.put("busPositionsCount", busPositionsCount);
            response.put("message", "Base de données accessible");
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Erreur de connexion à la base de données: " + e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}