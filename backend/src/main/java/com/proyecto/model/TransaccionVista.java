package com.proyecto.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Una transacción lista para mostrar: ya trae los números de cuenta y el
 * código de comprobante (en la tabla solo están los ids).
 * Es un "record": un objeto de solo lectura, ideal para respuestas JSON.
 */
public record TransaccionVista(
        int id,
        String tipo,
        BigDecimal monto,
        LocalDateTime fecha,
        String cuentaOrigen,
        String cuentaDestino,
        String comprobante) { }
