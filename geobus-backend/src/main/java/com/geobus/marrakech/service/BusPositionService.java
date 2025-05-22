package com.geobus.marrakech.service;

import java.time.LocalDateTime;
import java.util.List;

import com.geobus.marrakech.model.BusPosition;

/**
 * Interface pour les services liés aux positions des bus
 */
public interface BusPositionService {

    /**
     * Récupère les positions récentes des bus d'une ligne spécifique
     *
     * @param ligne Numéro/Nom de la ligne de bus
     * @param minutes Nombre de minutes dans le passé à considérer
     * @return Liste des positions récentes
     */
    List<BusPosition> getRecentBusPositions(String ligne, int minutes);

    /**
     * Récupère la dernière position connue d'un bus spécifique
     *
     * @param busId Identifiant du bus
     * @return Dernière position connue ou null si aucune position n'est trouvée
     */
    BusPosition getLastBusPosition(String busId);
}