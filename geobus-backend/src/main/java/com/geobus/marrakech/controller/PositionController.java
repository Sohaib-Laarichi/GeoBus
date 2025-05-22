package com.geobus.marrakech.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.geobus.marrakech.model.BusPosition;
import com.geobus.marrakech.repository.BusPositionRepository;

/**
 * Contrôleur pour les endpoints /positions/**
 * Utilisé par l'application mobile
 */
@RestController
@RequestMapping("/positions")
@CrossOrigin(origins = "*")
public class PositionController {

    @Autowired
    private BusPositionRepository busPositionRepository;

    @Autowired
    private com.geobus.marrakech.repository.StopRepository stopRepository;

    // Alternative: utiliser le service si disponible
    // @Autowired
    // private BusPositionService busPositionService;

    /**
     * Test simple - ne nécessite aucune dépendance
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Endpoint /positions/test fonctionne !");
    }

    /**
     * Récupère toutes les positions récentes
     */
    @GetMapping
    public ResponseEntity<List<BusPosition>> getAllPositions(
            @RequestParam(value = "minutes", defaultValue = "30") int minutes) {

        try {
            LocalDateTime since = LocalDateTime.now().minusMinutes(minutes);
            List<BusPosition> positions = busPositionRepository.findByTimestampAfter(since);

            if (positions.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            return ResponseEntity.ok(positions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Récupère les bus pour une station spécifique
     */
    @GetMapping("/stop/{stopId}")
    public ResponseEntity<List<BusPosition>> getBusesForStop(@PathVariable Long stopId) {
        try {
            System.out.println("Recherche des bus pour la station ID: " + stopId);

            // Récupérer la station par son ID
            com.geobus.marrakech.model.Stop stop = stopRepository.findById(stopId)
                    .orElse(null);

            if (stop == null) {
                System.out.println("Station avec ID " + stopId + " non trouvée");
                return ResponseEntity.notFound().build();
            }

            System.out.println("Station trouvée: " + stop.getStopName() + " [" + stop.getLatitude() + ", " + stop.getLongitude() + "]");

            // Récupérer toutes les positions récentes (augmenter la fenêtre de temps)
            LocalDateTime since = LocalDateTime.now().minusYears(10); // Augmenter considérablement la fenêtre de temps pour les tests
            List<BusPosition> allPositions = busPositionRepository.findByTimestampAfter(since);

            System.out.println("Nombre total de positions récentes: " + allPositions.size());

            if (allPositions.isEmpty()) {
                System.out.println("Aucune position de bus trouvée, récupération de toutes les positions");
                // Si aucune position récente, récupérer toutes les positions
                allPositions = busPositionRepository.findAll();
                System.out.println("Nombre total de positions (toutes): " + allPositions.size());

                if (allPositions.isEmpty()) {
                    System.out.println("Aucune position de bus trouvée dans la base de données");
                    return ResponseEntity.noContent().build();
                }
            }

            // Filtrer les bus qui sont proches de la station (rayon de 5000m)
            final double MAX_DISTANCE = 5000.0; // 5000 mètres (5km) au lieu de 500m
            List<BusPosition> nearbyBuses = allPositions.stream()
                    .filter(bus -> {
                        double distance = stop.distanceTo(bus.getLatitude(), bus.getLongitude());
                        System.out.println("Bus " + bus.getBusId() + " à " + distance + "m de la station");
                        return distance <= MAX_DISTANCE;
                    })
                    .collect(Collectors.toList());

            System.out.println("Nombre de bus à proximité (rayon " + MAX_DISTANCE + "m): " + nearbyBuses.size());

            if (nearbyBuses.isEmpty()) {
                // Si aucun bus n'est proche, retourner tous les bus comme fallback
                nearbyBuses = allPositions;
                System.out.println("Aucun bus à proximité, retour de tous les bus comme fallback (" + nearbyBuses.size() + " bus)");
            }

            System.out.println("Retour de " + nearbyBuses.size() + " positions de bus");
            return ResponseEntity.ok(nearbyBuses);
        } catch (Exception e) {
            System.out.println("Erreur lors de la recherche des bus: " + e.getMessage());
            e.printStackTrace(); // Pour debug
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Endpoint de debug pour tester les problèmes de réponse
     */
    @GetMapping("/stop/{stopId}/debug")
    public ResponseEntity<String> debugStopEndpoint(@PathVariable Long stopId) {
        try {
            return ResponseEntity.ok("Debug endpoint for stop " + stopId + " working correctly");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Compte le nombre total de positions
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getPositionsCount() {
        try {
            long count = busPositionRepository.count();
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Liste toutes les stations disponibles
     */
    @GetMapping("/stops")
    public ResponseEntity<List<com.geobus.marrakech.model.Stop>> getAllStops() {
        try {
            List<com.geobus.marrakech.model.Stop> stops = stopRepository.findAll();
            System.out.println("Nombre total de stations: " + stops.size());
            for (com.geobus.marrakech.model.Stop stop : stops) {
                System.out.println("Station ID " + stop.getStopId() + ": " + stop.getStopName() + 
                    " [" + stop.getLatitude() + ", " + stop.getLongitude() + "]");
            }
            return ResponseEntity.ok(stops);
        } catch (Exception e) {
            System.out.println("Erreur lors de la récupération des stations: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
