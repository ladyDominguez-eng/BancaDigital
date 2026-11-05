package com.proyecto.util;

/**
 * Se intentó una acción que el estado actual no permite (por ejemplo aprobar
 * un crédito que ya fue rechazado). ManejadorErrores la convierte en 409.
 */
public class TransicionInvalidaException extends RuntimeException {

    public TransicionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
