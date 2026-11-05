package com.proyecto.model;

import java.math.BigDecimal;

/** Datos que envía el cliente al solicitar un crédito. */
public record SolicitudCreditoRequest(BigDecimal monto, int plazoMeses) { }
