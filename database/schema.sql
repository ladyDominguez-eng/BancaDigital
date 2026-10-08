-- ========================================================
-- PROYECTO BANCA DIGITAL - SCRIPT DE BASE DE DATOS
-- Base de datos: Banca_BD
-- ========================================================

-- 1. Tabla de Usuarios
CREATE TABLE IF NOT EXISTS usuario (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(30) NOT NULL, -- 'ADMIN', 'CAJERO', 'CLIENTE', 'ASESOR', 'AUDITOR'
    activo BOOLEAN DEFAULT TRUE NOT NULL
);

-- 2. Tabla de Parámetros del Sistema
CREATE TABLE IF NOT EXISTS parametro_sistema (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) UNIQUE NOT NULL,
    valor VARCHAR(100) NOT NULL,
    descripcion TEXT
);

-- 3. Tabla de Bitácora de Acceso (Auditoría - Patrón Observer)
CREATE TABLE IF NOT EXISTS bitacora_acceso (
    id SERIAL PRIMARY KEY,
    usuario_id INT NOT NULL REFERENCES usuario(id) ON DELETE CASCADE,
    accion VARCHAR(255) NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 4. Tabla de Clientes
CREATE TABLE IF NOT EXISTS cliente (
    id SERIAL PRIMARY KEY,
    dni VARCHAR(20) UNIQUE NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    telefono VARCHAR(20),
    usuario_id INT REFERENCES usuario(id) ON DELETE SET NULL
);

-- 5. Tabla de Cuentas
CREATE TABLE IF NOT EXISTS cuenta (
    id SERIAL PRIMARY KEY,
    numero_cuenta VARCHAR(20) UNIQUE NOT NULL,
    tipo VARCHAR(30) NOT NULL DEFAULT 'AHORRO', -- 'AHORRO', 'CORRIENTE'
    saldo NUMERIC(12, 2) NOT NULL DEFAULT 0.00,
    moneda VARCHAR(10) NOT NULL DEFAULT 'PEN',
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA', -- 'ACTIVA', 'BLOQUEADA', 'INACTIVA'
    cliente_id INT NOT NULL REFERENCES cliente(id) ON DELETE CASCADE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 6. Tabla de Transacciones (Operaciones de Cajero / Ventanilla)
CREATE TABLE IF NOT EXISTS transaccion (
    id SERIAL PRIMARY KEY,
    cuenta_id INT NOT NULL REFERENCES cuenta(id) ON DELETE CASCADE,
    tipo VARCHAR(30) NOT NULL, -- 'DEPOSITO', 'RETIRO', 'TRANSFERENCIA'
    monto NUMERIC(12, 2) NOT NULL,
    saldo_resultante NUMERIC(12, 2),
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    descripcion VARCHAR(255),
    usuario_id INT REFERENCES usuario(id) ON DELETE SET NULL -- Cajero o usuario que operó
);

-- ========================================================
-- DATOS INICIALES / SEMILLAS DE PRUEBA
-- Contraseña para todos los usuarios de prueba: 123456
-- ========================================================

-- Usuarios con hash BCrypt de '123456'
INSERT INTO usuario (id, username, password_hash, rol, activo) VALUES
(1, 'admin', '$2a$10$6WmX0aFyDMGgCyCoTOs2GuxDLwA7Zq/b9C.DQjq3yg700yMRlWs5G', 'ADMIN', true),
(2, 'cajero1', '$2a$10$6WmX0aFyDMGgCyCoTOs2GuxDLwA7Zq/b9C.DQjq3yg700yMRlWs5G', 'CAJERO', true),
(3, 'cliente1', '$2a$10$6WmX0aFyDMGgCyCoTOs2GuxDLwA7Zq/b9C.DQjq3yg700yMRlWs5G', 'CLIENTE', true),
(4, 'asesor1', '$2a$10$6WmX0aFyDMGgCyCoTOs2GuxDLwA7Zq/b9C.DQjq3yg700yMRlWs5G', 'ASESOR', true),
(5, 'auditor1', '$2a$10$6WmX0aFyDMGgCyCoTOs2GuxDLwA7Zq/b9C.DQjq3yg700yMRlWs5G', 'AUDITOR', true)
ON CONFLICT (id) DO NOTHING;

SELECT setval('usuario_id_seq', (SELECT MAX(id) FROM usuario));

-- Parámetros del sistema
INSERT INTO parametro_sistema (id, nombre, valor, descripcion) VALUES
(1, 'MONTO_MAX_DEPOSITO_VENTANILLA', '10000.00', 'Monto máximo permitido por depósito en ventanilla'),
(2, 'MONTO_MAX_RETIRO_VENTANILLA', '5000.00', 'Monto máximo permitido por retiro en ventanilla'),
(3, 'COMISION_RETIRO_OTRA_PLAZA', '5.00', 'Comisión fija por retiro fuera de la plaza de origen'),
(4, 'TASA_INTERES_AHORRO_ANUAL', '0.035', 'Tasa efectiva anual para cuentas de ahorros')
ON CONFLICT (id) DO NOTHING;

SELECT setval('parametro_sistema_id_seq', (SELECT MAX(id) FROM parametro_sistema));

-- Clientes de prueba
INSERT INTO cliente (id, dni, nombres, apellidos, email, telefono, usuario_id) VALUES
(1, '71234567', 'Juan Carlos', 'Pérez Quispe', 'juan.perez@example.com', '987654321', 3),
(2, '72345678', 'María Elena', 'García Flores', 'maria.garcia@example.com', '912345678', NULL),
(3, '73456789', 'Carlos Alberto', 'Rodríguez Gómez', 'carlos.rodriguez@example.com', '955443322', NULL)
ON CONFLICT (id) DO NOTHING;

SELECT setval('cliente_id_seq', (SELECT MAX(id) FROM cliente));

-- Cuentas bancarias de prueba
INSERT INTO cuenta (id, numero_cuenta, tipo, saldo, moneda, estado, cliente_id) VALUES
(1, '191-100200300-01', 'AHORRO', 2500.00, 'PEN', 'ACTIVA', 1),
(2, '191-100200300-02', 'CORRIENTE', 820.50, 'PEN', 'ACTIVA', 1),
(3, '191-200300400-01', 'AHORRO', 5400.00, 'PEN', 'ACTIVA', 2),
(4, '191-300400500-01', 'AHORRO', 150.00, 'PEN', 'ACTIVA', 3)
ON CONFLICT (id) DO NOTHING;

SELECT setval('cuenta_id_seq', (SELECT MAX(id) FROM cuenta));

-- Transacciones iniciales para informes
INSERT INTO transaccion (cuenta_id, tipo, monto, saldo_resultante, descripcion, usuario_id) VALUES
(1, 'DEPOSITO', 1500.00, 2500.00, 'Depósito inicial en ventanilla', 2),
(2, 'DEPOSITO', 500.00, 820.50, 'Apertura de cuenta corriente', 2),
(3, 'DEPOSITO', 5400.00, 5400.00, 'Depósito en efectivo', 2),
(1, 'RETIRO', 200.00, 2300.00, 'Retiro en ventanilla por cliente', 2);

-- Bitácora inicial
INSERT INTO bitacora_acceso (usuario_id, accion) VALUES
(1, 'INICIO_SESION'),
(2, 'INICIO_SESION');
