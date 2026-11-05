package com.proyecto.model;

/**
 * Valores posibles del estado de una cuenta.
 *
 * PROBLEMA: el texto "ACTIVA" estaba escrito a mano en el servicio y dentro
 *           de la consulta SQL. Un error de escritura ("Activa", "ACTIVO")
 *           no daba error de compilación y cambiaba el resultado
 *           (antipatrón "números/valores mágicos").
 * SOLUCIÓN: una constante con nombre, definida en un único lugar.
 */
public final class EstadoCuenta {

    public static final String ACTIVA = "ACTIVA";

    private EstadoCuenta() { }
}
