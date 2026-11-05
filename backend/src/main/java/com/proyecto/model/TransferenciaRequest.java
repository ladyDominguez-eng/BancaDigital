package com.proyecto.model;

import java.math.BigDecimal;

/** Datos que envía el cliente para transferir (cuenta propia -> número de cuenta destino). */
public record TransferenciaRequest(int cuentaOrigenId, String numeroDestino, BigDecimal monto) { }
