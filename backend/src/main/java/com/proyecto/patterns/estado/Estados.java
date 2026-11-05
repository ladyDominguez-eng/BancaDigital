package com.proyecto.patterns.estado;

import java.util.List;

/**
 * Fábrica de estados: convierte el nombre guardado en la base de datos en el
 * objeto de estado correspondiente (patrón Factory, igual que CuentaFactory).
 * Los nombres válidos coinciden con la restricción CHECK de la tabla
 * solicitud_credito.
 */
public final class Estados {

    public static final List<String> NOMBRES =
            List.of("PENDIENTE", "EN_EVALUACION", "APROBADO", "RECHAZADO");

    private Estados() { }

    public static EstadoSolicitud desdeNombre(String nombre) {
        if (nombre == null) {
            return new Pendiente();
        }
        return switch (nombre) {
            case "PENDIENTE" -> new Pendiente();
            case "EN_EVALUACION" -> new EnEvaluacion();
            case "APROBADO" -> new Aprobado();
            case "RECHAZADO" -> new Rechazado();
            default -> throw new IllegalArgumentException("Estado de solicitud desconocido: " + nombre);
        };
    }
}
