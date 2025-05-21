package com.geobus.marrakech.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.geobus.marrakech.dto.StopDTO;
import com.geobus.marrakech.dto.TimeEstimationDTO;
import com.geobus.marrakech.model.Stop;
import com.geobus.marrakech.repository.StopRepository;

import jakarta.persistence.EntityNotFoundException;

/**
 * Implémentation des services liés aux stations de bus
 */
@Service
public class StopServiceImpl implements StopService {

    @Autowired
    private StopRepository stopRepository;

    @Override
    public List<StopDTO> getAllStopsInMarrakech() {
        List<Stop> stops = stopRepository.findByVilleIgnoreCase("Marrakech");
        return stops.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public StopDTO getNearestStop(Double lat, Double lon) {
        Stop nearestStop = stopRepository.findNearestStop(lat, lon);
        if (nearestStop == null) {
            return null;
        }

        StopDTO stopDTO = convertToDTO(nearestStop);
        double distance = nearestStop.distanceTo(lat, lon);
        double walkingTime = nearestStop.estimateWalkingTime(lat, lon);

        stopDTO.setDistance(distance);
        stopDTO.setWalkingTimeMinutes(walkingTime);

        return stopDTO;
    }

    @Override
    public TimeEstimationDTO getTimeToStop(Double userLat, Double userLon, Long stopId) {
        Stop stop = stopRepository.findById(stopId)
                .orElseThrow(() -> new EntityNotFoundException("Station non trouvée avec l'ID: " + stopId));

        double distance = stop.distanceTo(userLat, userLon);
        double walkingTime = stop.estimateWalkingTime(userLat, userLon);

        return new TimeEstimationDTO(
                stop.getStopId(),
                stop.getStopName(),
                distance,
                walkingTime
        );
    }

    /**
     * Convertit une entité Stop en DTO
     */
    private StopDTO convertToDTO(Stop stop) {
        return new StopDTO(
                stop.getStopId(),
                stop.getStopName(),
                stop.getLatitude(),
                stop.getLongitude(),
                stop.getVille(),
                null, // distance - à calculer si nécessaire
                null  // temps de marche - à calculer si nécessaire
        );
    }
}