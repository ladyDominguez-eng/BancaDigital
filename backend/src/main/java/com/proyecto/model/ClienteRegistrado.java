package com.proyecto.model;

/**
 * Resultado de registrar un cliente. "comprobante" solo viene si hubo un
 * depósito inicial (es el código del movimiento que lo respalda).
 */
public record ClienteRegistrado(
        int usuarioId,
        int clienteId,
        int cuentaId,
        String numeroCuenta,
        String comprobante) {
}
