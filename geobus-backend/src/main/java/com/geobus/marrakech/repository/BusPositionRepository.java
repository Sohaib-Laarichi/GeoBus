package com.geobus.marrakech.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.geobus.marrakech.model.BusPosition;

/**
 * Repository pour accéder aux données de position des bus
 */
@Repository
public interface BusPositionRepository extends JpaRepository<BusPosition, Long> {

    /**
     * Trouve les positions récentes des bus d'une ligne spécifique
     *
     * @param ligne Numéro/Nom de la ligne de bus
     * @param since Date/heure à partir de laquelle chercher
     * @return Liste des positions récentes
     */
    List<BusPosition> findByLigneAndTimestampAfterOrderByTimestampDesc(String ligne, LocalDateTime since);

    /**
     * Trouve la dernière position connue d'un bus spécifique
     *
     * @param busId Identifiant du bus
     * @return Dernière position connue
     */
    BusPosition findFirstByBusIdOrderByTimestampDesc(String busId);
}