package com.geobus.marrakech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principale pour démarrer l'application GeoBus Marrakech
 * Cette application fournit des API pour localiser les stations de bus à Marrakech
 *
 * @author GeoBus Team
 */
@SpringBootApplication
public class GeoBusApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeoBusApplication.class, args);
    }
}