-- ============================================================================
-- SCRIPT SQL COMPLET ET FINAL POUR GEOBUS MARRAKECH
-- ============================================================================

-- Création de la base de données (si elle n'existe pas déjà)
CREATE DATABASE IF NOT EXISTS geobus_marrakech
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Utilisation de la base de données
USE geobus_marrakech;

-- Suppression des tables existantes pour éviter les conflits (dans l'ordre des dépendances)
DROP TABLE IF EXISTS bus_positions;
DROP TABLE IF EXISTS stops;
DROP TABLE IF EXISTS users;

-- ============================================================================
-- TABLE USERS - Utilisateurs de l'application
-- ============================================================================
CREATE TABLE users (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       username VARCHAR(50) NOT NULL,
                       email VARCHAR(100) NOT NULL,
                       password VARCHAR(120) NOT NULL,
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       PRIMARY KEY (id),
                       UNIQUE KEY uk_users_username (username),
                       UNIQUE KEY uk_users_email (email),
                       KEY idx_users_username (username),
                       KEY idx_users_email (email),
                       KEY idx_users_created_at (created_at)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
    COMMENT='Table des utilisateurs de l\'application GeoBus';

-- ============================================================================
-- TABLE STOPS - Stations de bus
-- ============================================================================
CREATE TABLE stops (
                       stop_id BIGINT NOT NULL AUTO_INCREMENT,
                       stop_name VARCHAR(255) NOT NULL,
                       latitude DOUBLE NOT NULL,
                       longitude DOUBLE NOT NULL,
                       ville VARCHAR(255) NOT NULL,

                       PRIMARY KEY (stop_id),
                       KEY idx_stops_ville (ville),
                       KEY idx_stops_location (latitude, longitude),
                       KEY idx_stops_name (stop_name)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
    COMMENT='Table des stations de bus';

-- ============================================================================
-- TABLE BUS_POSITIONS - Positions des bus en temps réel
-- ============================================================================
CREATE TABLE bus_positions (
                               id BIGINT NOT NULL AUTO_INCREMENT,
                               bus_id VARCHAR(255) NOT NULL,
                               latitude DOUBLE NOT NULL,
                               longitude DOUBLE NOT NULL,
                               ligne VARCHAR(255) NOT NULL,
                               timestamp DATETIME NOT NULL,

                               PRIMARY KEY (id),
                               KEY idx_bus_positions_bus_id (bus_id),
                               KEY idx_bus_positions_ligne (ligne),
                               KEY idx_bus_positions_timestamp (timestamp),
                               KEY idx_bus_positions_location (latitude, longitude)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
    COMMENT='Table des positions des bus en temps réel';

-- ============================================================================
-- INSERTION DES DONNÉES DE TEST
-- ============================================================================

-- Insertion d'utilisateurs de test avec mots de passe BCrypt
-- Mot de passe pour tous: "password123"
INSERT INTO users (username, email, password, created_at) VALUES
                                                              ('testuser', 'test@example.com', '$2a$10$XptfWj/QrP8F9wK5Rd2pz.Hjj1TG/IfJz6VJlbRZ/nRq1.lLH5yOq', NOW()),
                                                              ('admin', 'admin@geobus.com', '$2a$10$XptfWj/QrP8F9wK5Rd2pz.Hjj1TG/IfJz6VJlbRZ/nRq1.lLH5yOq', NOW()),
                                                              ('user1', 'user1@example.com', '$2a$10$XptfWj/QrP8F9wK5Rd2pz.Hjj1TG/IfJz6VJlbRZ/nRq1.lLH5yOq', NOW());

-- Insertion des stations de bus à Marrakech
INSERT INTO stops (stop_name, latitude, longitude, ville) VALUES
-- Centre-ville et lieux emblématiques
('Gare Routière', 31.6295, -7.9811, 'Marrakech'),
('Jemaa el-Fna', 31.6258, -7.9891, 'Marrakech'),
('Bab Doukkala', 31.6333, -7.9913, 'Marrakech'),
('Gueliz', 31.6372, -8.0027, 'Marrakech'),
('Majorelle', 31.6417, -8.0031, 'Marrakech'),
('Mcdonalds Gueliz', 31.6386, -8.0121, 'Marrakech'),
('Hôpital Ibn Tofail', 31.6353, -8.0148, 'Marrakech'),
('Avenue Mohammed V', 31.6324, -7.9971, 'Marrakech'),
('Koutoubia', 31.6242, -7.9885, 'Marrakech'),

-- Quartiers résidentiels
('Al Massira', 31.6140, -8.0162, 'Marrakech'),
('Semlalia', 31.6523, -8.0044, 'Marrakech'),
('Daoudiate', 31.6407, -7.9712, 'Marrakech'),
('Hay Mohammadi', 31.6183, -7.9678, 'Marrakech'),
('Ménara', 31.6105, -8.0234, 'Marrakech'),
('Palmeraie', 31.6491, -7.9541, 'Marrakech'),
('Bab Aghmat', 31.6167, -7.9799, 'Marrakech'),
('Sidi Youssef Ben Ali', 31.6104, -7.9888, 'Marrakech'),

-- Destinations touristiques
('Jardin Majorelle', 31.6414, -8.0031, 'Marrakech'),
('Palais Bahia', 31.6216, -7.9828, 'Marrakech'),
('Musée Yves Saint Laurent', 31.6412, -8.0035, 'Marrakech'),
('Palais El Badi', 31.6178, -7.9827, 'Marrakech'),
('Jardins de Ménara', 31.6100, -8.0300, 'Marrakech'),
('Musée de Marrakech', 31.6318, -7.9869, 'Marrakech'),
('Tombeaux Saadiens', 31.6184, -7.9851, 'Marrakech'),
('Medina', 31.6295, -7.9871, 'Marrakech'),

-- Stations supplémentaires pour un réseau complet
('Aéroport Marrakech-Ménara', 31.6069, -8.0363, 'Marrakech'),
('Université Cadi Ayyad', 31.6634, -8.0089, 'Marrakech'),
('Centre Commercial Al Mazar', 31.6698, -8.0105, 'Marrakech'),
('Hôpital Militaire', 31.6445, -8.0234, 'Marrakech'),
('Bab Ighli', 31.6401, -7.9634, 'Marrakech'),
('Cyber Park', 31.6311, -7.9895, 'Marrakech');

-- Insertion des positions de bus de test
INSERT INTO bus_positions (bus_id, latitude, longitude, ligne, timestamp) VALUES
-- Ligne 1 (Rouge) - Centre-ville vers Gueliz
('BUS001', 31.6295, -7.9811, 'Ligne 1', NOW() - INTERVAL 2 MINUTE),
('BUS002', 31.6324, -7.9971, 'Ligne 1', NOW() - INTERVAL 5 MINUTE),
('BUS003', 31.6372, -8.0027, 'Ligne 1', NOW() - INTERVAL 1 MINUTE),

-- Ligne 2 (Bleue) - Médina vers Palmeraie
('BUS004', 31.6258, -7.9891, 'Ligne 2', NOW() - INTERVAL 3 MINUTE),
('BUS005', 31.6417, -8.0031, 'Ligne 2', NOW() - INTERVAL 7 MINUTE),
('BUS006', 31.6491, -7.9541, 'Ligne 2', NOW() - INTERVAL 4 MINUTE),

-- Ligne 3 (Verte) - Aéroport vers Centre
('BUS007', 31.6069, -8.0363, 'Ligne 3', NOW() - INTERVAL 8 MINUTE),
('BUS008', 31.6105, -8.0234, 'Ligne 3', NOW() - INTERVAL 6 MINUTE),
('BUS009', 31.6242, -7.9885, 'Ligne 3', NOW() - INTERVAL 1 MINUTE),

-- Ligne 4 (Jaune) - Université vers Hôpitaux
('BUS010', 31.6634, -8.0089, 'Ligne 4', NOW() - INTERVAL 9 MINUTE),
('BUS011', 31.6353, -8.0148, 'Ligne 4', NOW() - INTERVAL 2 MINUTE),
('BUS012', 31.6445, -8.0234, 'Ligne 4', NOW() - INTERVAL 5 MINUTE);

-- ============================================================================
-- VÉRIFICATIONS ET STATISTIQUES
-- ============================================================================

-- Affichage du nombre d'enregistrements créés
SELECT
    'users' as table_name,
    COUNT(*) as record_count
FROM users
UNION ALL
SELECT
    'stops' as table_name,
    COUNT(*) as record_count
FROM stops
UNION ALL
SELECT
    'bus_positions' as table_name,
    COUNT(*) as record_count
FROM bus_positions;

-- Vérification de la structure des tables


-- Test de requêtes typiques
SELECT 'Test de connexion utilisateur' as test_description;
SELECT id, username, email, created_at
FROM users
WHERE username = 'testuser';

SELECT 'Test de recherche de stations' as test_description;
SELECT stop_id, stop_name, latitude, longitude
FROM stops
WHERE ville = 'Marrakech'
LIMIT 5;

SELECT 'Test de positions de bus récentes' as test_description;
SELECT bus_id, ligne, latitude, longitude, timestamp
FROM bus_positions
WHERE timestamp >= NOW() - INTERVAL 10 MINUTE
ORDER BY timestamp DESC;

-- ============================================================================
-- COMMANDES UTILES POUR LA MAINTENANCE
-- ============================================================================

-- Pour nettoyer les anciennes positions de bus (à exécuter périodiquement)
-- DELETE FROM bus_positions WHERE timestamp < NOW() - INTERVAL 24 HOUR;

-- Pour ajouter un nouvel utilisateur (exemple)
-- INSERT INTO users (username, email, password, created_at) VALUES
-- ('nouveau_user', 'nouveau@example.com', '$2a$10$...', NOW());

-- Pour ajouter une nouvelle station
-- INSERT INTO stops (stop_name, latitude, longitude, ville) VALUES
-- ('Nouvelle Station', 31.6000, -8.0000, 'Marrakech');

-- Pour mettre à jour la position d'un bus
-- INSERT INTO bus_positions (bus_id, latitude, longitude, ligne, timestamp) VALUES
-- ('BUS013', 31.6300, -7.9900, 'Ligne 1', NOW());

COMMIT;

-- Message de fin
SELECT 'Base de données GeoBus Marrakech créée avec succès!' as message;