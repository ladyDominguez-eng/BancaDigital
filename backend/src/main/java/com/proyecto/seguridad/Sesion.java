package com.proyecto.seguridad;

import java.time.Instant;

/**
 * Datos del usuario que inició sesión. Se guarda en el servidor asociado a
 * un token; el frontend solo conoce el token, nunca estos datos.
 */
public record Sesion(int usuarioId, String username, String rol, Instant creada) {

    public Sesion(int usuarioId, String username, String rol) {
        this(usuarioId, username, rol, Instant.now());
    }
}
