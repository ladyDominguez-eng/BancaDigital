package com.proyecto.model;

/**
 * Servicios que el cliente puede pagar (RF: pagar servicios).
 *
 * PROBLEMA: escribir "LUZ", "AGUA"... como texto suelto en varios lugares
 *           permite errores de escritura que nadie detecta al compilar.
 * SOLUCIÓN: una enumeración; el código del servicio solo puede ser uno de
 *           estos valores, y agregar uno nuevo es una sola línea.
 */
public enum ServicioPago {

    LUZ("Luz"),
    AGUA("Agua"),
    INTERNET("Internet"),
    TELEFONO("Teléfono");

    private final String nombre;

    ServicioPago(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    /** Convierte el texto recibido del frontend; si no existe, error 400. */
    public static ServicioPago desde(String codigo) {
        if (codigo != null) {
            for (ServicioPago s : values()) {
                if (s.name().equalsIgnoreCase(codigo.trim())) {
                    return s;
                }
            }
        }
        throw new IllegalArgumentException("El servicio indicado no existe.");
    }
}
