package com.proyecto.model;

import java.math.BigDecimal;

/**
 * Datos que envía el Administrador para dar de alta a un cliente nuevo:
 * su acceso (usuario y contraseña), sus datos personales y su primera cuenta.
 */
public record RegistrarClienteRequest(
        String username,
        String password,
        String dni,
        String nombres,
        String apellidos,
        String tipoCuenta,
        BigDecimal saldoInicial) {
}
