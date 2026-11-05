package com.proyecto.util;

import com.proyecto.model.Cuenta;
import com.proyecto.model.EstadoCuenta;
import java.math.BigDecimal;

/**
 * Validaciones de negocio compartidas.
 *
 * PROBLEMA: las mismas comprobaciones (monto positivo, cuenta activa, saldo
 *           suficiente) estaban a punto de escribirse en el servicio del
 *           cliente y en el del cajero (copiar y pegar).
 * SOLUCIÓN: se escriben una sola vez aquí y todos las reutilizan. Si la
 *           regla cambia (por ejemplo, admitir 3 decimales) se cambia en
 *           un solo lugar.
 * Todas lanzan IllegalArgumentException, que ManejadorErrores convierte en 400.
 */
public final class Validaciones {

    private Validaciones() { }

    public static void montoValido(BigDecimal monto) {
        if (monto == null || monto.signum() <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor que cero.");
        }
        if (monto.scale() > 2) {
            throw new IllegalArgumentException("El monto admite como máximo 2 decimales.");
        }
    }

    // ---- Datos de acceso y de persona (los comparten el alta de usuarios y el de clientes) ----

    /** 4 a 30 caracteres: letras, números, punto, guion y guion bajo (sin espacios). */
    public static void usernameValido(String username) {
        if (username == null || !username.matches("[A-Za-z0-9._-]{4,30}")) {
            throw new IllegalArgumentException(
                    "El usuario debe tener de 4 a 30 caracteres: letras, números, punto, guion o guion bajo.");
        }
    }

    /** Mínimo 8 caracteres: es la regla básica de una contraseña aceptable. */
    public static void claveValida(String password) {
        if (password == null || password.length() < 8 || password.length() > 72) {
            // 72 es el máximo que BCrypt tiene en cuenta
            throw new IllegalArgumentException("La contraseña debe tener entre 8 y 72 caracteres.");
        }
    }

    public static void dniValido(String dni) {
        if (dni == null || !dni.matches("\\d{8}")) {
            throw new IllegalArgumentException("El DNI debe tener exactamente 8 dígitos.");
        }
    }

    /** @param campo nombre que verá el usuario en el mensaje, por ejemplo "Los nombres" */
    public static void nombreValido(String texto, String campo) {
        if (texto == null || !texto.trim().matches("[\\p{L} ']{2,60}")) {
            throw new IllegalArgumentException(
                    campo + " debe tener de 2 a 60 letras (sin números ni símbolos).");
        }
    }

    /** @param descripcion por ejemplo "de origen", para el mensaje de error */
    public static void cuentaActiva(Cuenta c, String descripcion) {
        if (!EstadoCuenta.ACTIVA.equals(c.getEstado())) {
            throw new IllegalArgumentException("La cuenta " + descripcion + " no está activa.");
        }
    }

    /** El saldo después de restar el monto no puede bajar del mínimo de la cuenta. */
    public static void saldoSuficiente(Cuenta c, BigDecimal monto) {
        if (c.getSaldo().subtract(monto).compareTo(c.getSaldoMinimo()) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente.");
        }
    }
}
