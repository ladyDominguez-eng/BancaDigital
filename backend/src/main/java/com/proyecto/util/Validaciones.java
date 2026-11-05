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
