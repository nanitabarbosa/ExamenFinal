CREATE TABLE tipo_vehiculo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL
);

INSERT INTO tipo_vehiculo (id, nombre) VALUES (1, 'Moto');
INSERT INTO tipo_vehiculo (id, nombre) VALUES (2, 'Carro');

CREATE TABLE tarifa(
    id INT AUTO_INCREMENT PRIMARY KEY,
    monto INT NOT NULL,
    tiempo_hora INT NOT NULL
);

INSERT INTO tarifa(id, monto, tiempo_hora) values (1, 1000, 1);
INSERT INTO tarifa(id, monto, tiempo_hora) values (2, 2000, 1);

CREATE TABLE tarifa_tipo_vehiculo(
    id INT AUTO_INCREMENT PRIMARY KEY,
    tarifa_id INT,
    tipo_vehiculo_id INT,
    FOREIGN KEY (tarifa_id) REFERENCES tarifa(id),
    FOREIGN KEY (tipo_vehiculo_id) REFERENCES tipo_vehiculo(id)
);

INSERT INTO tarifa_tipo_vehiculo (tarifa_id, tipo_vehiculo_id) VALUES (1, 1);
INSERT INTO tarifa_tipo_vehiculo (tarifa_id, tipo_vehiculo_id) VALUES (2, 2);

CREATE TABLE vehiculo (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tipo_vehiculo_id INT,
    placa VARCHAR(20) NOT NULL,
    marca VARCHAR(100) NOT NULL,
    modelo VARCHAR(100) NOT NULL,
    nombre_propietario VARCHAR(255) NOT NULL,
    FOREIGN KEY (tipo_vehiculo_id) REFERENCES tipo_vehiculo(id)
);

CREATE TABLE aparcamiento (
    id INT AUTO_INCREMENT PRIMARY KEY,
    vehiculo_id INT,
    tiempo_total DECIMAL(10, 2),
    monto_total DECIMAL(10, 2),
    fecha_entrada DATETIME NOT NULL,
    fecha_salida DATETIME,
    activo BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (vehiculo_id) REFERENCES vehiculo(id)
);
