package com.geobus.marrakech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour transférer les données des stations de bus à l'application mobile
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StopDTO {
    private Long stopId;
    private String stopName;
    private Double latitude;
    private Double longitude;
    private String ville;

    // Attributs additionnels pour les stations les plus proches
    private Double distance;
    private Double walkingTimeMinutes;
}