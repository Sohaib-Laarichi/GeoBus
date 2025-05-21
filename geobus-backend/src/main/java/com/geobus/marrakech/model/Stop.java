package com.geobus.marrakech.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entité représentant une station de bus
 */
@Entity
@Table(name = "stops")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stopId;

    @NotBlank
    private String stopName;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    @NotBlank
    private String ville;

    /**
     * Calcule la distance entre cette station et les coordonnées données
     * en utilisant la formule de Haversine (distance sur la surface d'une sphère)
     *
     * @param lat Latitude du point à comparer
     * @param lon Longitude du point à comparer
     * @return Distance en mètres
     */
    public double distanceTo(double lat, double lon) {
        final int R = 6371; // Rayon de la Terre en km
        double latDistance = Math.toRadians(lat - this.latitude);
        double lonDistance = Math.toRadians(lon - this.longitude);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(this.latitude)) * Math.cos(Math.toRadians(lat))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // Distance en mètres

        return distance;
    }

    /**
     * Estime le temps nécessaire pour marcher jusqu'à cette station
     * en considérant une vitesse moyenne de marche de 5 km/h
     *
     * @param lat Latitude du point de départ
     * @param lon Longitude du point de départ
     * @return Temps estimé en minutes
     */
    public double estimateWalkingTime(double lat, double lon) {
        double distance = distanceTo(lat, lon);
        // Considérer une vitesse moyenne de marche de 5 km/h
        double walkingSpeedMetersPerMinute = 5 * 1000 / 60; // 5km/h en m/min
        return distance / walkingSpeedMetersPerMinute;
    }
}