-- Création des tables
CREATE TABLE IF NOT EXISTS stops (
                                     stop_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     stop_name VARCHAR(255) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    ville VARCHAR(255) NOT NULL
    );

CREATE TABLE IF NOT EXISTS bus_positions (
                                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             bus_id VARCHAR(255) NOT NULL,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    ligne VARCHAR(255) NOT NULL,
    timestamp DATETIME NOT NULL
    );

-- Insertion des données d'exemple pour les stations de bus à Marrakech
INSERT INTO stops (stop_name, latitude, longitude, ville) VALUES
-- Centre-ville
('Gare Routière', 31.6295, -7.9811, 'Marrakech'),
('Jemaa el-Fna', 31.6258, -7.9891, 'Marrakech'),
('Bab Doukkala', 31.6333, -7.9913, 'Marrakech'),
('Gueliz', 31.6372, -8.0027, 'Marrakech'),
('Majorelle', 31.6417, -8.0031, 'Marrakech'),
('Mcdonalds Gueliz', 31.6386, -8.0121, 'Marrakech'),
('Hôpital Ibn Tofail', 31.6353, -8.0148, 'Marrakech'),
('Avenue Mohammed V', 31.6324, -7.9971, 'Marrakech'),
('Koutoubia', 31.6242, -7.9885, 'Marrakech');

-- Quartiers résidentiels
INSERT INTO stops (stop_name, latitude, longitude, ville) VALUES
('Al Massira', 31.6140, -8.0162, 'Marrakech'),
('Semlalia', 31.6523, -8.0044, 'Marrakech'),
('Daoudiate', 31.6407, -7.9712, 'Marrakech'),
('Hay Mohammadi', 31.6183, -7.9678, 'Marrakech'),
('Ménara', 31.6105, -8.0234, 'Marrakech'),
('Palmeraie', 31.6491, -7.9541, 'Marrakech'),
('Bab Aghmat', 31.6167, -7.9799, 'Marrakech'),
('Sidi Youssef Ben Ali', 31.6104, -7.9888, 'Marrakech');

-- Destinations touristiques
INSERT INTO stops (stop_name, latitude, longitude, ville) VALUES
('Jardin Majorelle', 31.6414, -8.0031, 'Marrakech'),
('Palais Bahia', 31.6216, -7.9828, 'Marrakech'),
('Musée Yves Saint Laurent', 31.6412, -8.0035, 'Marrakech'),
('Palais El Badi', 31.6178, -7.9827, 'Marrakech'),
('Jardins de Ménara', 31.6100, -8.0300, 'Marrakech'),
('Musée de Marrakech', 31.6318, -7.9869, 'Marrakech'),
('Tombeaux Saadiens', 31.6184, -7.9851, 'Marrakech'),
('Medina', 31.6295, -7.9871, 'Marrakech');
