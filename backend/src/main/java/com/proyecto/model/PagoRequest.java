package com.proyecto.model;

import java.math.BigDecimal;

/** Datos que envía el cliente para pagar un servicio desde una de sus cuentas. */
public record PagoRequest(int cuentaId, String servicio, BigDecimal monto) { }
