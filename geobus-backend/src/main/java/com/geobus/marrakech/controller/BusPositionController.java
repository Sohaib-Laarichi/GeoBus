package com.geobus.marrakech.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.geobus.marrakech.model.BusPosition;
import com.geobus.marrakech.service.BusPositionService;

/**
 * Contrôleur REST pour gérer les requêtes liées aux positions des bus
 */
@RestController
@RequestMapping("/buses")
@CrossOrigin(origins = "*", maxAge = 3600)
public class BusPositionController {

    @Autowired
    private BusPositionService busPositionService;

    /**
     * Récupère les positions récentes des bus d'une ligne spécifique
     *
     * @param ligne Numéro/Nom de la ligne de bus
     * @param minutes Nombre de minutes dans le passé à considérer (par défaut 30)
     * @return Liste des positions récentes
     */
    @GetMapping("/line/{ligne}/positions")
    public ResponseEntity<List<BusPosition>> getRecentBusPositions(
            @PathVariable("ligne") String ligne,
            @RequestParam(value = "minutes", defaultValue = "30") int minutes) {

        try {
            List<BusPosition> positions = busPositionService.getRecentBusPositions(ligne, minutes);
            if (positions.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(positions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère la dernière position connue d'un bus spécifique
     *
     * @param busId Identifiant du bus
     * @return Dernière position connue
     */
    @GetMapping("/{busId}/position")
    public ResponseEntity<BusPosition> getLastBusPosition(
            @PathVariable("busId") String busId) {

        try {
            BusPosition position = busPositionService.getLastBusPosition(busId);
            if (position == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(position);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint simple pour tester l'accès aux bus
     */
    @GetMapping("/test")
    public ResponseEntity<String> testBusesEndpoint() {
        return ResponseEntity.ok("Endpoint des bus accessible !");
    }
}