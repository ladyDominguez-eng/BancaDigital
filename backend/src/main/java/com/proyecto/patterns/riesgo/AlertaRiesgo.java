package com.proyecto.patterns.riesgo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Una operación marcada como sospechosa por alguna regla de riesgo. */
public record AlertaRiesgo(
        String regla,
        String descripcion,
        int transaccionId,
        String cuenta,
        BigDecimal monto,
        LocalDateTime fecha) { }
