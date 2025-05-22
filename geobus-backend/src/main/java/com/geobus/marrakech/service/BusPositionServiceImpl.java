package com.geobus.marrakech.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.geobus.marrakech.model.BusPosition;
import com.geobus.marrakech.repository.BusPositionRepository;

/**
 * Implémentation des services liés aux positions des bus
 */
@Service
public class BusPositionServiceImpl implements BusPositionService {

    @Autowired
    private BusPositionRepository busPositionRepository;

    @Override
    public List<BusPosition> getRecentBusPositions(String ligne, int minutes) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(minutes);
        return busPositionRepository.findByLigneAndTimestampAfterOrderByTimestampDesc(ligne, since);
    }

    @Override
    public BusPosition getLastBusPosition(String busId) {
        return busPositionRepository.findFirstByBusIdOrderByTimestampDesc(busId);
    }

    /**
     * Méthodes supplémentaires utiles
     */
    public BusPosition saveBusPosition(BusPosition busPosition) {
        if (busPosition.getTimestamp() == null) {
            busPosition.setTimestamp(LocalDateTime.now());
        }
        return busPositionRepository.save(busPosition);
    }

    public List<BusPosition> getAllRecentPositions(int minutes) {
        LocalDateTime since = LocalDateTime.now().minusMinutes(minutes);
        return busPositionRepository.findByTimestampAfter(since);
    }

    public void cleanOldPositions(int hours) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        List<BusPosition> oldPositions = busPositionRepository.findByTimestampBefore(cutoff);
        busPositionRepository.deleteAll(oldPositions);
    }
}