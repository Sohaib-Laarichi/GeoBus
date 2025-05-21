package com.geobus.marrakech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour renvoyer le temps estimé pour atteindre une station de bus
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeEstimationDTO {
    private Long stopId;
    private String stopName;
    private Double distance;
    private Double walkingTimeMinutes;
}