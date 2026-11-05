package com.proyecto.model;

/**
 * Tipos de transacción permitidos por la base de datos.
 * Antipatrón evitado: valores mágicos (texto suelto repetido en el código).
 */
public final class TipoTransaccion {

    public static final String DEPOSITO = "DEPOSITO";
    public static final String RETIRO = "RETIRO";
    public static final String TRANSFERENCIA = "TRANSFERENCIA";
    public static final String PAGO_SERVICIO = "PAGO_SERVICIO";

    private TipoTransaccion() { }
}
